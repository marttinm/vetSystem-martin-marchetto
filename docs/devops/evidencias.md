# Evidencias del TP de DevOps

Registro de capturas, salidas de comandos y decisiones tomadas durante el trabajo.
Las capturas se guardan en `docs/devops/img/`.

## Fase 2 — Gestión de cambios y versionado

### Estrategia de branching
Se adoptó Trunk-Based Development: `main` es la única rama permanente y cada cambio se trabaja en una rama corta que se integra mediante Pull Request.
La rama `materia-devops`, usada al inicio, se integró a `main` por PR y desde entonces se trabaja con ramas como `ci/github-actions`.

![PR #8 mergeado en main](img/pr-docker-mergeado.png)

### Protección de la rama `main`
Se configuró una regla de protección sobre `main` que exige Pull Request para integrar cambios y que los checks de CI (`lint / Checkstyle` y `test / Tests`) pasen antes del merge. No se permite saltear la regla, ni siquiera al administrador.

**Problema encontrado**: la regla figuraba como "Not enforced". En el plan gratuito de GitHub las reglas de protección solo se aplican en repositorios públicos, y el repositorio era privado. El PR del pipeline de CI se integró antes de detectarlo. Se cambió la visibilidad del repositorio a público (requisito del TP), tras verificar que el historial no contuviera credenciales.

- Captura de la regla como "Not enforced" con el repositorio privado: no se tomó en su momento y no se puede reproducir sin volver a hacer privado el repositorio.

![Regla de protección de main con los checks obligatorios](img/proteccion-main.png)

Además de los checks, la regla exige que la rama esté actualizada con `main` antes del merge, así los tests corren sobre el código que efectivamente se va a integrar.

Configuración actual de la regla, consultada con la API de GitHub:

```
$ gh api repos/marttinm/vetSystem-martin-marchetto/branches/main/protection
required_pull_request_reviews: activo (0 aprobaciones)
required_status_checks: ["lint / Checkstyle", "test / Tests"] (strict: true)
enforce_admins: true
allow_force_pushes: false
allow_deletions: false
```

### Verificación del historial antes de hacer público el repositorio
Se buscaron credenciales en todo el historial:

```bash
git --no-pager log -p --all | grep -iE "password|secret|token" | grep -v "DB_PASSWORD"
```

Resultado: solo aparecen variables del wrapper de Maven (`MVNW_PASSWORD`), la contraseña del PostgreSQL efímero del pipeline (`ci`) y la contraseña por defecto (`root`) de la base local usada antes de la migración desde MySQL. Ninguna es una credencial real. El archivo `.env` nunca se versionó.

### Conventional Commits
El historial sigue la convención a partir del commit `83789ee`. Los dos commits iniciales (`b58134b` y `d80ccf3`) son previos a su adopción y no se reescribieron para no alterar el historial ya publicado.

Esos tres primeros commits (`b58134b`, `d80ccf3` y `83789ee`, de agosto) también entraron directo a `main`, sin Pull Request. Corresponden al inicio del proyecto en la materia de Microservicios (sprints 1 y 2), antes de empezar el TP: a partir del [PR #1](https://github.com/marttinm/vetSystem-martin-marchetto/pull/1)) todos los cambios se integraron por Pull Request.

Hay una excepción posterior: `420ab8b` ("Enhance README with project details and setup instructions"), una edición del README hecha desde la interfaz web de GitHub. Además de no seguir la convención, se integró directo en `main` sin Pull Request, porque en ese momento la regla de protección de `main` todavía no se aplicaba. Tampoco se reescribió: hacerlo implicaría un force push sobre `main`, que ya está publicada y protegida. Desde que la regla se aplica, cualquier push directo a `main` es rechazado.

## Fase 3 — Docker

### Dockerfile
- Multi-stage: etapa de build con `maven:3.9.9-eclipse-temurin-21-alpine` y etapa final solo con `eclipse-temurin:21-jre-alpine`.
- Sin tag `latest`: la versión de Java y de Maven está fijada en el tag de cada imagen base.
- Se copia primero el `pom.xml` y se ejecuta `mvn dependency:go-offline` antes de copiar `src`, para cachear las dependencias.
- La app corre con el usuario sin privilegios `spring`.

Salida de `docker compose exec app whoami`:

```
$ docker compose exec app whoami
spring
```

Salida de `docker images` con el tamaño de la imagen final (`v1.0.1`):

```
REPOSITORY:TAG                               SIZE
vetsystem-martin-marchetto-app:latest        266MB
maven:3.9.9-eclipse-temurin-21-alpine        386MB
```

La imagen final pesaba 266 MB en la `v1.0.1`; en la `v1.1.2`, con las dependencias de métricas que se agregaron en la `v1.1.0`, pesa 272 MB. Solo la imagen base de la etapa de build (Maven + JDK) pesa 386 MB, sin contar las dependencias descargadas ni el código compilado: el multi-stage evita que todo eso llegue a la imagen que se ejecuta. En Docker Hub la imagen comprimida pesa 120,7 MB en la `v1.0.1` y 125,7 MB en la `v1.1.2`.

**Rebuild tras modificar código**: se agregó un comentario a `VetSystemApplication.java` y se reconstruyó la imagen con `docker compose build --progress=plain app`:

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

Salida de `docker compose ps` con ambos servicios corriendo:

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

Salida de `./mvnw checkstyle:check` en verde tras las correcciones:

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

![Checks del PR #10 en verde y obligatorios](img/ci-checks-verde.png)

![Historial de ejecuciones en GitHub Actions](img/actions-historial.png)

La captura del PR bloqueado por un check fallido está en [Experimento de falla controlada](#experimento-de-falla-controlada).

### Publicación en Docker Hub

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

En el panel de Render, el deploy `dep-dbbcje8m7kps73eg44m0` (el mismo id que devolvió el hook) figura con la versión `1.0.1` y el Deploy Hook como origen:

![Deploy de la 1.0.1 en Render, lanzado por el Deploy Hook](img/render-deploy-v1.0.1.png)


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

### Logs estructurados en JSON ([PR #21](https://github.com/marttinm/vetSystem-martin-marchetto/pull/21))
- Se usa el logging estructurado nativo de Spring Boot (`logging.structured.format.console=logstash`), sin dependencias nuevas. `@timestamp` se renombra a `timestamp` y se excluyen `@version` y `level_value`.
- `RequestLoggingFilter` escribe una línea por request con `method`, `path`, `status_code` y `duration_ms` como campos propios del JSON.
- Se desactivaron el banner de Spring y `spring.jpa.show-sql`, que escriben texto plano directo en la consola y rompían el formato.

```json
{"timestamp":"2026-10-20T02:27:31.04013043Z","message":"GET /no-existe 404","logger_name":"com.vetSystem.config.RequestLoggingFilter","thread_name":"http-nio-8080-exec-3","level":"INFO","method":"GET","path":"/no-existe","status_code":404,"duration_ms":27}
```

Validación en el contenedor: las 29 líneas del log de la app, desde el arranque hasta los requests, son JSON válido.

### Métricas hacia Grafana Cloud ([PR #22](https://github.com/marttinm/vetSystem-martin-marchetto/pull/22) y [PR #23](https://github.com/marttinm/vetSystem-martin-marchetto/pull/23))
- **Plataforma**: Grafana Cloud (plan Free), una de las cuatro opciones de la consigna.
- **Envío**: Micrometer mide cada request en `http.server.requests` y el registry OTLP lo envía cada 30 segundos directo al endpoint OTLP de Grafana Cloud. No hace falta un agente ni un collector.
- **Configuración**: variables de entorno `OTLP_METRICS_ENABLED`, `OTLP_METRICS_URL` y `OTLP_METRICS_AUTH`. Si no se definen, el envío queda apagado (CI y entorno local). En Render se cargan como variables del servicio.
- **Histograma**: `http.server.requests` publica buckets entre 5 ms y 10 s para calcular p50 y p95.
- En Grafana, la métrica llega como `http_server_requests_milliseconds_count` / `_bucket`, con las etiquetas `job="vet-system"`, `method`, `uri`, `status` y `outcome`.

### Generador de tráfico
`scripts/generar-trafico.sh` envía pedidos a la API mezclando respuestas 200, 400, 404 y 405. Un pedido fallido (timeout, corte de conexión) se cuenta como `000` y no corta el script.

```
$ ./scripts/generar-trafico.sh https://vet-system-1-0-0.onrender.com 900
Generando trafico contra https://vet-system-1-0-0.onrender.com durante 900 segundos...
HTTP 000: 5 pedidos
HTTP 200: 870 pedidos
HTTP 400: 290 pedidos
HTTP 404: 290 pedidos
HTTP 405: 291 pedidos
```

### Dashboard "VetSystem - Golden Signals"
Diseñado desde cero, sin plantillas de Grafana. Cada panel se definió con su propia consulta PromQL, que se probó contra los datos reales antes de crear el dashboard por la API de Grafana. Se actualiza cada 30 segundos.

| Panel | Golden Signal | Consulta |
| --- | --- | --- |
| Tráfico (requests por minuto) | Tráfico | `sum by (status) (rate(http_server_requests_milliseconds_count{job="vet-system", uri!~"/actuator.*"}[2m])) * 60` |
| Latencia (p50 / p95) | Latencia | `histogram_quantile(0.95, sum by (le) (rate(http_server_requests_milliseconds_bucket{job="vet-system", uri!~"/actuator.*"}[5m])))` (y 0.50 para el p50) |
| Errores 4xx (por minuto) | Errores | `sum by (status) (rate(http_server_requests_milliseconds_count{job="vet-system", status=~"4.."}[2m])) * 60` |
| Errores 5xx (por minuto) | Errores | `sum(rate(http_server_requests_milliseconds_count{job="vet-system", status=~"5.."}[2m])) * 60 or vector(0)` |

- `uri!~"/actuator.*"` excluye los chequeos de salud para que no se cuenten como tráfico de la API.
- `rate(...)[2m] * 60` convierte el contador acumulado en requests por minuto. La ventana de 2 minutos cubre al menos cuatro envíos de 30 segundos.
- `histogram_quantile` calcula el percentil a partir de los buckets del histograma.
- `or vector(0)` hace que el panel de 5xx muestre 0 en lugar de "No data" cuando no hay errores.

![Dashboard con las Golden Signals](img/grafana-dashboard-golden-signals.png)

### Alerta de errores 5xx
- **Regla** "Errores 5xx en la API" (carpeta `VetSystem`): usa la misma consulta que el panel de 5xx y se dispara si hay más de 0 errores 5xx por minuto durante al menos 1 minuto. Etiquetas `severity=critical` y `service=vet-system`.
- **Notificación**: las alertas con `service=vet-system` se envían por email (punto de contacto `email-vet-system`), incluyendo el aviso de resolución.
- **Si la evaluación falla**: la regla mantiene su último estado (`KeepLast`) en lugar de pasar a *Error*. Así una falla de conexión de Grafana no se confunde con una falla de la API (ver Problemas encontrados).

### Problemas encontrados
- **El exportador OTLP no se activaba** ([PR #23](https://github.com/marttinm/vetSystem-martin-marchetto/pull/23)): con la `v1.1.0` desplegada, las métricas no llegaban a Grafana, aunque un dato de prueba enviado a mano con el mismo token sí aparecía. El reporte de condiciones de Actuator mostró la causa:

  ```
  negativeMatches OtlpMetricsExportAutoConfiguration
      OnClassCondition - @ConditionalOnClass did not find required class 'org.springframework.boot.opentelemetry.autoconfigure.OpenTelemetryProperties'
  ```

  En Spring Boot 4 hace falta el módulo `spring-boot-opentelemetry`. Sin él, la autoconfiguración se descarta sin registrar ningún error: la app no enviaba nada y tampoco avisaba. Se agregó la dependencia y el test `MetricasOtlpTest`, que verifica que el registry OTLP exista cuando el envío está activado. Es una falla silenciosa que solo se detectó mirando la plataforma de monitoreo.
- **Token sin `Basic`**: el header de autenticación tiene que ser `Basic <token>`. Con el token solo, Grafana responde 401.
- **Arranque lento en Render**: la métrica `application_ready_time` marcó 138 segundos de arranque con la CPU del plan Free. Cada deploy tarda unos 2 minutos en completarse (mientras tanto sigue respondiendo la versión anterior), y el servicio también tarda en despertar después de 15 minutos sin tráfico.
- **Falsas alarmas por errores de evaluación**: el historial de la alerta mostró más de 20 transiciones a *Pending (Error)* y *Error* en unas dos horas, con el error `dial tcp ...:443: i/o timeout` al consultar el Prometheus de Grafana Cloud. La falla era de la conexión interna de la plataforma, no de la API. Con la configuración por defecto, cada falla pasaba la regla a *Error* y podía enviar un email aunque la app funcionara bien. Una alerta que suena sin motivo deja de tomarse en serio, así que se configuró la regla para mantener su último estado cuando la evaluación falla.
- **El generador de tráfico se cortaba**: con `set -e`, un pedido fallido terminaba el script. Se cambió para que cuente el fallo como `000` y siga.

## Andon Cord (dónde se corta el pipeline)
- **Linter**: si Checkstyle encuentra violaciones, el job `lint / Checkstyle` falla.
- **Tests**: si algún test falla, el job `test / Tests` falla.
- **Regla de protección de `main`**: ambos checks son obligatorios y la regla no admite excepciones, por lo que con cualquiera de los dos en rojo el botón de merge queda deshabilitado y el cambio no llega a `main`.
- **Release**: el workflow de release vuelve a correr lint y tests sobre el código del tag. Si alguno falla, no se publica la imagen en Docker Hub ni se despliega en Render.
- **En producción**: la alerta de errores 5xx avisa por email cuando la API desplegada empieza a fallar, aunque el código haya pasado todos los checks.

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

### Errores del cliente reportados como 500, detectados por el monitoreo
**Qué se rompió**: se envió tráfico a la API desplegada en Render pidiendo recursos con ids que no son números (`GET /api/duenios/abc`). El objetivo era comprobar si el monitoreo detectaba los errores sin intervención manual.

**Cómo reaccionó el sistema** (horarios en UTC):

| Hora | Evento |
| --- | --- |
| 20:59 | Empieza el tráfico con ids inválidos (106 respuestas 500 en 5 minutos). |
| 21:01:10 | La evaluación de la regla falla por un error de conexión de Grafana Cloud y la alerta queda en *Pending (Error)*. |
| 21:02:10 | La evaluación siguiente detecta 19,3 errores 5xx por minuto y la alerta pasa a *Firing* y envía el email. |

**Resultado**: la alerta mostró un error real: cuando un parámetro no se puede convertir al tipo esperado, Spring lanza `MethodArgumentTypeMismatchException` y el handler genérico la devolvía como 500. Se corrigió en el [PR #24](https://github.com/marttinm/vetSystem-martin-marchetto/pull/24), que responde 400 e incluye un test.

**Verificación del fix** (release `v1.1.2`, 20/10): mientras Render reemplazaba la versión, se consultó `/api/duenios/abc` cada 15 segundos. La versión anterior seguía respondiendo 500, así que la alerta volvió a dispararse y después se resolvió sola cuando entró la nueva versión:

| Hora | Evento |
| --- | --- |
| 22:41:28 | La versión anterior responde 500 a `/api/duenios/abc`. |
| 22:43:10 | La alerta pasa a *Pending*. |
| 22:44:01 | La `1.1.2` ya está activa y responde 400: `El valor 'abc' no es valido para el parametro id`. |
| 22:44:10 | La alerta pasa a *Firing*: la ventana de 2 minutos todavía incluye los 500 anteriores. |
| 22:46:10 | Sin errores 5xx en la ventana, la alerta vuelve a *Normal*. |

El panel de Render muestra el deploy de la `1.1.2` lanzado por el Deploy Hook (2m41s). En los logs del servicio aparecen, en formato JSON, los pedidos a `/api/duenios/abc` respondidos con 400 por la versión nueva:

![Deploy de la 1.1.2 en Render, lanzado por el Deploy Hook](img/render-deploy-v1.1.2.png)

El historial muestra los horarios en GMT-3: la secuencia de la verificación aparece a las 19:43:10 (*Pending*), 19:44:10 (*Alerting*) y 19:46:10 (*Normal*). Las transiciones a *Pending (Error)* y *Error* de las horas anteriores son las fallas de evaluación descritas en [Problemas encontrados](#problemas-encontrados-2), y la última entrada (19:47:20, *Normal (Updated)*) es el cambio de la regla a `KeepLast`.

![Historial de la alerta de errores 5xx](img/grafana-alerta-historial.png)

Cuando la alerta pasa a *Firing*, Grafana envía un email con la cantidad de errores detectados. El primero corresponde al experimento (19,3 errores 5xx por minuto, observados a las 21:02:10 UTC) y el segundo a la verificación de la `1.1.2` (4,2 errores por minuto a las 22:44:10 UTC):

![Emails de la alerta en estado Firing](img/grafana-alerta-email-firing.png)

Cuando la alerta se resuelve, Grafana envía otro email con el aviso de resolución:

![Emails de resolución de la alerta](img/grafana-alerta-email-resuelta.png)

**Conclusión**: el monitoreo cierra el ciclo de feedback (Segunda Forma). Los tests no cubrían este caso, pero la alerta lo detectó en producción y el aprendizaje volvió al código como un test nuevo (Tercera Forma).

## Desperdicios Lean
Desarrollados en el informe técnico ([README](../../README.md#4-principios-lean-reducción-de-desperdicio)).

## Decisiones tomadas
- **Versionado SemVer**: lo exige la Fase 4 para etiquetar la imagen en Docker Hub. Además es coherente con Conventional Commits (`feat` sube el MINOR, `fix` el PATCH).
- **Trunk-Based Development**: una sola rama permanente y ramas cortas por cambio. Es el modelo más simple para un proyecto individual y favorece entregas en lotes pequeños.
- **Repositorio público sin aprobaciones obligatorias**: quien no tenga permisos de escritura solo puede proponer cambios desde un fork. No se exige aprobación de reviews porque GitHub no permite aprobar PRs propios y, siendo un único desarrollador, bloquearía todos los merges. El control de calidad lo garantizan los checks obligatorios.
- **Configuración por variables de entorno**: `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` con valores por defecto locales. El mismo código funciona sin cambios en local, en Docker y en CI.
- **Paridad de versiones**: Java 21 y PostgreSQL 14 en local, en el contenedor y en el pipeline, para que los entornos sean equivalentes.
- **Postgres en el pipeline**: `VetSystemApplicationTests` levanta el contexto completo de Spring y necesita una base de datos. En CI se usa un service container de PostgreSQL en lugar de eliminar el test.
- **Reglas de Checkstyle propias**: se eligieron reglas que detectan problemas reales en lugar de adoptar una configuración externa completa.
- **Render como plataforma de despliegue**: es gratuito y sus Deploy Hooks aceptan el parámetro `imgURL`, que permite desplegar una versión exacta tal como pide la consigna. Otras plataformas, como DigitalOcean App Platform, cobran por contenedores y no tienen deploy hooks, por lo que habría que desplegar con su API.
- **Grafana Cloud como plataforma de monitoreo**: su plan gratuito no vence antes de la defensa, recibe OTLP directo desde la app sin agregar un agente a la imagen (importante con los 512 MB del plan Free de Render) y los paneles se definen con PromQL.
- **Workflows reutilizables**: lint y tests se definen una sola vez y se invocan desde el workflow de CI; el workflow de release reutiliza los mismos tests sin duplicar configuración.