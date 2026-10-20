# Evidencias del TP de DevOps

Registro de capturas, salidas de comandos y decisiones para armar el informe final.
Las capturas se guardan en `docs/devops/img/`.

## Fase 2 — Gestión de cambios y versionado

### Estrategia de branching
Se adoptó Trunk-Based Development: `main` es la única rama permanente y cada cambio se trabaja en una rama corta que se integra mediante Pull Request.
La rama `materia-devops`, usada al inicio, se integró a `main` por PR y desde entonces se trabaja con ramas como `ci/github-actions`.

- [x] Captura del PR de Docker mergeado

![PR #8 mergeado en main](img/pr-docker-mergeado.png)

### Protección de la rama `main`
Se configuró una regla de protección sobre `main` que exige Pull Request para integrar cambios y que los checks de CI (`lint / Checkstyle` y `test / Tests`) pasen antes del merge. No se permite saltear la regla, ni siquiera al administrador.

**Problema encontrado**: la regla figuraba como "Not enforced". En el plan gratuito de GitHub las reglas de protección solo se aplican en repositorios públicos, y el repositorio era privado. El PR del pipeline de CI se integró antes de detectarlo. Se cambió la visibilidad del repositorio a público (requisito del TP), tras verificar que el historial no contuviera credenciales.

- Captura de la regla como "Not enforced" con el repositorio privado: no se tomó en su momento y no se puede reproducir sin volver a hacer privado el repositorio.
- [ ] Captura de la regla aplicada, con los checks obligatorios

Configuración actual de la regla, consultada con la API de GitHub:

```
$ gh api repos/marttinm/vetSystem-martin-marchetto/branches/main/protection
required_pull_request_reviews: activo (0 aprobaciones)
required_status_checks: ["lint / Checkstyle", "test / Tests"]
enforce_admins: true
allow_force_pushes: false
```

### Verificación del historial antes de hacer público el repositorio
Se buscaron credenciales en todo el historial:

```bash
git --no-pager log -p --all | grep -iE "password|secret|token" | grep -v "DB_PASSWORD"
```

Resultado: solo aparecen variables del wrapper de Maven (`MVNW_PASSWORD`), la contraseña del PostgreSQL efímero del pipeline (`ci`) y la contraseña por defecto (`root`) de la base local usada antes de la migración desde MySQL. Ninguna es una credencial real. El archivo `.env` nunca se versionó.

### Conventional Commits
El historial sigue la convención a partir del commit `83789ee`. Los dos commits iniciales (`b58134b` y `d80ccf3`) son previos a su adopción y no se reescribieron para no alterar el historial ya publicado.

Hay una excepción posterior: `420ab8b` ("Enhance README with project details and setup instructions"), una edición del README hecha desde la interfaz web de GitHub. Además de no seguir la convención, se integró directo en `main` sin Pull Request, porque en ese momento la regla de protección de `main` todavía no se aplicaba. Tampoco se reescribió: hacerlo implicaría un force push sobre `main`, que ya está publicada y protegida. Desde que la regla se aplica, cualquier push directo a `main` es rechazado.

## Fase 3 — Docker

### Dockerfile
- Multi-stage: etapa de build con `maven:3.9.9-eclipse-temurin-21-alpine` y etapa final solo con `eclipse-temurin:21-jre-alpine`.
- Sin tag `latest`, versiones fijadas.
- Se copia primero el `pom.xml` y se ejecuta `mvn dependency:go-offline` antes de copiar `src`, para cachear las dependencias.
- La app corre con el usuario sin privilegios `spring`.

- [x] Salida de `docker compose exec app whoami` (debe responder `spring`)

```
$ docker compose exec app whoami
spring
```

- [x] Salida de `docker images` con el tamaño de la imagen final

```
REPOSITORY:TAG                               SIZE
vetsystem-martin-marchetto-app:latest        266MB
maven:3.9.9-eclipse-temurin-21-alpine        386MB
```

La imagen final pesa 266 MB. Solo la imagen base de la etapa de build (Maven + JDK) pesa 386 MB, sin contar las dependencias descargadas ni el código compilado: el multi-stage evita que todo eso llegue a la imagen que se ejecuta. En Docker Hub la imagen comprimida pesa 120,7 MB.

- [x] Rebuild tras modificar código, mostrando `mvn dependency:go-offline` como `CACHED`

Se agregó un comentario a `VetSystemApplication.java` y se reconstruyó la imagen con `docker compose build --progress=plain app`:

```
#10 [build 3/6] COPY pom.xml .
#10 CACHED
#11 [build 4/6] RUN mvn dependency:go-offline -B
#11 CACHED
#12 [build 5/6] COPY src ./src
#13 [build 6/6] RUN mvn package -DskipTests -B
#14 [runtime 2/4] WORKDIR /app
#14 CACHED
#15 [runtime 3/4] RUN addgroup -S spring && adduser -S spring -G spring
#15 CACHED
#16 [runtime 4/4] COPY --from=build --chown=spring:spring /app/target/*.jar app.jar
```

Como el `pom.xml` no cambió, la descarga de dependencias sale de la caché; solo se vuelven a ejecutar la copia del código y la compilación.

### docker-compose
Levanta la API y PostgreSQL con `docker compose up --build`. La base tiene healthcheck y la app espera a que esté `healthy` antes de arrancar. Los datos persisten en un volumen.

- [x] Captura de `docker compose ps` con ambos servicios corriendo

```
SERVICE   IMAGE                            STATUS                   PORTS
app       vetsystem-martin-marchetto-app   Up Less than a second    0.0.0.0:8080->8080/tcp
db        postgres:14.23-alpine            Up 6 seconds (healthy)   0.0.0.0:5433->5432/tcp
```

### Problemas encontrados
- **Contexto de build vacío (`transferring context: 2B`)**: el Dockerfile estaba en la raíz del repo, pero el proyecto Maven está en `vet-system/`. Se movieron el Dockerfile y el `.dockerignore` a esa carpeta y se configuró `context: ./vet-system` en el compose.
- **Versión de PostgreSQL distinta entre entornos** ([PR #14](https://github.com/marttinm/vetSystem-martin-marchetto/pull/14)): el compose usaba `postgres:16.4-alpine`, mientras que el CI y el desarrollo local usan PostgreSQL 14. Se alineó el compose a `14.23-alpine`. Un volumen creado con la versión 16 no puede leerse con la 14, por lo que hubo que recrearlo con `docker compose down -v`.
- **Postgres no arrancaba**: la imagen oficial exige `POSTGRES_PASSWORD` no vacío. El `.env` tenía `DB_PASSWORD=` vacío. Se definió una contraseña; la configuración local sin Docker no se ve afectada porque Spring no lee el `.env`.

## Fase 4 — CI/CD

### Linter: Checkstyle (2026-10-04)
Se agregó Checkstyle con una configuración propia (`vet-system/checkstyle.xml`) enfocada en problemas reales, en lugar de las configuraciones de Google o Sun, que reportan cientos de violaciones de formato.

Primera ejecución de `./mvnw checkstyle:check`: 19 errores y `BUILD FAILURE`.

```
[INFO] There are 19 errors reported by Checkstyle 10.20.1 with checkstyle.xml ruleset.
[ERROR] src/main/java/com/vetSystem/Dto/DuenioDTO.java:[4,38] (imports) AvoidStarImport
[ERROR] src/main/java/com/vetSystem/Dto/TurnoRequestDTO.java:[4,38] (imports) AvoidStarImport
[ERROR] src/main/java/com/vetSystem/Dto/VeterinarioDTO.java:[4,38] (imports) AvoidStarImport
[ERROR] src/main/java/com/vetSystem/Dto/MascotaDTO.java:[3,38] (imports) AvoidStarImport
[ERROR] src/main/java/com/vetSystem/Dto/MedicamentoRequestDTO.java:[4,38] (imports) AvoidStarImport
[ERROR] src/main/java/com/vetSystem/Entity/Mascota.java:[3,27] (imports) AvoidStarImport
[ERROR] src/main/java/com/vetSystem/Entity/Turno.java:[3,27] (imports) AvoidStarImport
[ERROR] src/main/java/com/vetSystem/Entity/Veterinario.java:[3,27] (imports) AvoidStarImport
[ERROR] src/main/java/com/vetSystem/Entity/Duenio.java:[3,27] (imports) AvoidStarImport
[ERROR] src/main/java/com/vetSystem/Entity/Duenio.java:[4,14] (imports) AvoidStarImport
[ERROR] src/main/java/com/vetSystem/Entity/Medicamento.java:[3,27] (imports) AvoidStarImport
[ERROR] src/main/java/com/vetSystem/Controller/VeterinarioController.java:[10,47] (imports) AvoidStarImport
[ERROR] src/main/java/com/vetSystem/Controller/DuenioController.java:[19,47] (imports) AvoidStarImport
[ERROR] src/main/java/com/vetSystem/Controller/TurnoController.java:[21,47] (imports) AvoidStarImport
[ERROR] src/main/java/com/vetSystem/Controller/MascotaController.java:[5,8] (imports) UnusedImports: Unused import - com.vetSystem.Entity.Mascota.
[ERROR] src/main/java/com/vetSystem/Controller/MascotaController.java:[11,47] (imports) AvoidStarImport
[ERROR] src/main/java/com/vetSystem/Controller/MedicamentoController.java:[11,47] (imports) AvoidStarImport
[ERROR] src/main/java/com/vetSystem/VetSystemApplication.java:[9,1] (whitespace) FileTabCharacter
[ERROR] src/test/java/com/vetSystem/VetSystemApplicationTests.java:[9,1] (whitespace) FileTabCharacter
[INFO] BUILD FAILURE
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-checkstyle-plugin:3.6.0:check (default-cli) on project vet-system: You have 19 Checkstyle violations.
```

Resolución:
- Se corrigieron los tabs (2 archivos) y el import sin uso en `MascotaController`.
- Se quitó la regla `AvoidStarImport`: los imports con `*` son una preferencia de estilo, no generan bugs y son la convención del proyecto. Mantenerla agregaba ruido sin valor.

- [x] Captura de `./mvnw checkstyle:check` en verde tras las correcciones

```
[INFO] --- checkstyle:3.6.0:check (default-cli) @ vet-system ---
[INFO] You have 0 Checkstyle violations.
[INFO] BUILD SUCCESS
```

### Workflow de CI
Se ejecuta en cada Pull Request hacia `main`. Está organizado en workflows reutilizables (`on: workflow_call`):
- `lint.yml`: Checkstyle.
- `test.yml`: tests con un service container de PostgreSQL 14.
- `ci.yml`: orquestador que se dispara en el PR y llama a ambos.

Lint y tests corren en paralelo para dar feedback más rápido; como los dos son obligatorios, si cualquiera falla el PR se bloquea igual. El workflow usa permisos de solo lectura y cancela ejecuciones anteriores del mismo PR (`concurrency`).

- [x] Captura de los checks del PR #N en verde

![Checks del PR #10 en verde y obligatorios](img/ci-checks-verde.png)

- [x] Captura del historial de ejecuciones en la pestaña Actions

![Historial de ejecuciones en GitHub Actions](img/actions-historial.png)
- [x] Captura del PR bloqueado cuando falla un check

### Publicación en Docker Hub
- [x] Captura de la imagen publicada con su tag SemVer

![Tags 1.0.0 y 1.0.1 en Docker Hub](img/dockerhub-tags.png)

### Despliegue continuo en Render
El servicio corre en Render (plan Free) a partir de la imagen de Docker Hub: https://vet-system-1-0-0.onrender.com

- **Base de datos**: PostgreSQL 14 (plan Free) en la misma región que el servicio (Virginia). La app se conecta por el hostname interno, a través de la red privada de Render, en lugar de salir por internet.
- **Configuración**: las mismas variables de entorno que en local y en CI (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`), más `PORT=8080`.
- **Deploy Hook** ([PR #16](https://github.com/marttinm/vetSystem-martin-marchetto/pull/16)): el job `deploy` de `release.yml` corre después de publicar la imagen y llama al hook con el parámetro `imgURL`, que indica la imagen exacta de la release (`docker.io/martinmarchetto/vet-system:X.Y.Z`). La URL del hook incluye una clave que permite desplegar, por eso se guarda como secret (`RENDER_DEPLOY_HOOK`).

Flujo completo al pushear un tag `vX.Y.Z`: lint + tests → publicación en Docker Hub → deploy en Render y Release en GitHub.

#### Release `v1.0.1` (validación del pipeline completo)
Primera release con despliegue automático. Incluye los fixes de códigos HTTP de los PRs #17, #18 y #19 (ver abajo). Los cinco jobs terminaron en verde y Render respondió al hook con el id del deploy:

```
{"deploy":{"id":"dep-dbbcje8m7kps73eg44m0"}}
```

![Pipeline de la release v1.0.1](img/release-v1.0.1-pipeline.png)

![Release v1.0.1 en GitHub](img/release-v1.0.1-github.png)

Unos dos minutos después del hook, el servicio respondía con el comportamiento nuevo, lo que confirma que Render desplegó la imagen `1.0.1`:

| Pedido | Con la 1.0.0 | Con la 1.0.1 |
| --- | --- | --- |
| `GET /` | 500 | 404 |
| `POST /api/duenios` con JSON mal formado | 500 | 400 |
| `PATCH /api/duenios` | 500 | 405 |
| `GET /api/duenios` y Swagger UI | 200 | 200 |

- [ ] Captura del panel de Render con el deploy de la `1.0.1`

#### Problemas encontrados
- **Usuario `magic` en producción**: el primer deploy falló con `password authentication failed for user "magic"`. La variable `DB_USERNAME` no estaba bien cargada en Render y Spring usó el valor por defecto de `application.properties`, pensado para el entorno local. Es un caso real de "variable de entorno faltante": la app no avisó que faltaba la variable, sino que intentó conectarse con credenciales de otro entorno.
- **Nombre de la base con sufijo**: Render agrega un sufijo al nombre de la base (`vet_system_ml9g`), por lo que el `DB_URL` no puede armarse con el nombre elegido al crearla. Un error al copiarlo dio `database "vet_system_ml9" does not exist`.
- **Formato de la URL de conexión**: Render ofrece la URL como `postgresql://usuario:clave@host/base`, pero Spring necesita el formato JDBC (`jdbc:postgresql://host:5432/base`) y las credenciales por separado.

### Errores del cliente reportados como 5xx
Al probar el servicio desplegado se detectó que tres errores del cliente terminaban en el handler genérico de `GlobalExceptionHandler` y se respondían como 500:

| Caso | Excepción de Spring | Antes | Después | PR |
| --- | --- | --- | --- | --- |
| Ruta inexistente | `NoResourceFoundException` | 500 | 404 | [#17](https://github.com/marttinm/vetSystem-martin-marchetto/pull/17) |
| JSON mal formado | `HttpMessageNotReadableException` | 500 | 400 | [#18](https://github.com/marttinm/vetSystem-martin-marchetto/pull/18) |
| Método no soportado | `HttpRequestMethodNotSupportedException` | 500 | 405 | [#19](https://github.com/marttinm/vetSystem-martin-marchetto/pull/19) |

Además de la respuesta incorrecta al cliente, afectaban la observabilidad: cualquier pedido mal formado se contaba como error del servidor y distorsionaba la señal de errores de las Golden Signals. Cada fix incluye un test en `DuenioControllerTest` que fallaba antes del cambio.

## Fase 5 — Observabilidad

## Andon Cord (dónde se corta el pipeline)
- **Linter**: si Checkstyle encuentra violaciones, el job `lint / Checkstyle` falla.
- **Tests**: si algún test falla, el job `test / Tests` falla.
- **Regla de protección de `main`**: ambos checks son obligatorios y la regla no admite excepciones, por lo que con cualquiera de los dos en rojo el botón de merge queda deshabilitado y el cambio no llega a `main`.

## Experimento de falla controlada

### Test unitario roto a propósito ([PR #11](https://github.com/marttinm/vetSystem-martin-marchetto/pull/11))
**Qué se rompió**: en `DuenioServiceTest.getAllDuenios_cuandoHayDuenios_retornaLista` se cambió el valor esperado del nombre del dueño de `"Carlos"` a `"Carlos1"`, simulando un cambio que introduce un error de lógica.

**Cómo reaccionó el sistema**:
- El workflow de CI se ejecutó automáticamente al abrir el PR.
- El job `lint / Checkstyle` pasó, ya que el código cumple las reglas de estilo.
- El job `test / Tests` falló: la aserción esperaba `"Carlos1"` y el servicio devolvió `"Carlos"`. El log del job indica el test fallido y la diferencia entre ambos valores.
- GitHub marcó el PR en rojo y la regla de protección de `main` deshabilitó el merge.

**Resultado**: el cambio defectuoso no llegó a `main`. El PR se cerró sin integrar.

**Conclusión**: el Andon Cord funciona como se diseñó: un error detectado por los tests detiene el flujo antes de la integración, sin depender de una revisión manual.

![PR bloqueado por test fallido](img/falla-controlada-pr-bloqueado.png)

## Desperdicios Lean

## Decisiones tomadas
- **Versionado SemVer**: lo exige la Fase 4 para etiquetar la imagen en Docker Hub. Además es coherente con Conventional Commits (`feat` sube el MINOR, `fix` el PATCH).
- **Trunk-Based Development**: una sola rama permanente y ramas cortas por cambio. Es el modelo más simple para un proyecto individual y favorece entregas en lotes pequeños.
- **Repositorio público sin aprobaciones obligatorias**: solo el dueño tiene permisos de escritura, por lo que nadie más puede pushear ni mergear; terceros solo pueden proponer cambios desde un fork. No se exige aprobación de reviews porque GitHub no permite aprobar PRs propios y, siendo un único desarrollador, bloquearía todos los merges. El control de calidad lo garantizan los checks obligatorios.
- **Configuración por variables de entorno**: `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` con valores por defecto locales. El mismo código funciona sin cambios en local, en Docker y en CI.
- **Paridad de versiones**: Java 21 y PostgreSQL 14 en local, en el contenedor y en el pipeline, para que los entornos sean equivalentes.
- **Postgres en el pipeline**: `VetSystemApplicationTests` levanta el contexto completo de Spring y necesita una base de datos. En CI se usa un service container de PostgreSQL en lugar de eliminar el test.
- **Reglas de Checkstyle propias**: se eligieron reglas que detectan problemas reales en lugar de adoptar una configuración externa completa.
- **Render como plataforma de despliegue**: es gratuito y sus Deploy Hooks aceptan el parámetro `imgURL`, que permite desplegar una versión exacta tal como pide la consigna. Otras plataformas, como DigitalOcean App Platform, cobran por contenedores y no tienen deploy hooks, por lo que habría que desplegar con su API.
- **Workflows reutilizables**: lint y tests se definen una sola vez y se invocan desde el workflow de CI; el workflow de release va a reutilizar los mismos tests sin duplicar configuración.