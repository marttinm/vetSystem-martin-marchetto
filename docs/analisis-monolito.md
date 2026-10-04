# Análisis del monolito — VetSystem

Cierre de la Fase 1. VetSystem hoy es un único proceso Spring Boot (`vet-system.jar`) que contiene los cuatro módulos (Dueños, Mascotas, Veterinarios y Turnos) organizados en capas `Controller → Service → Repository`, y todos persisten en una única base de datos PostgreSQL (`vet_system`). Para el volumen actual de la clínica *Patitas Felices* esta arquitectura funciona bien: es simple de desarrollar, de testear y de deployar. Este documento analiza qué pasa cuando el sistema crece, respondiendo las cinco preguntas del Sprint 7.

## Qué funciona bien hoy

- **Simplicidad operativa:** un solo artefacto para compilar, un solo proceso para levantar y una sola base de datos para administrar.
- **Transacciones locales:** `TurnoService.createTurno()` valida mascota, veterinario y superposición y guarda el turno dentro de un mismo `@Transactional`. No hace falta coordinar nada entre servicios.
- **Llamadas en memoria:** cuando `TurnoService` necesita una mascota, llama a `MascotaRepository` directamente, sin red ni latencia.
- **Tests y errores centralizados:** un solo `GlobalExceptionHandler` y una sola suite de `mvn test` cubren toda la aplicación.

---

## 1. Escalabilidad acoplada — ¿qué pasa si solo queremos escalar Turnos?

**Problema.** En un monolito la unidad de escalado es la aplicación entera. No existe forma de darle más recursos solo al módulo de Turnos: si se levantan más instancias, cada una trae también Dueños, Mascotas y Veterinarios, aunque esos módulos no tengan carga.

**Ejemplo en la clínica.** Al empezar una campaña de vacunación antirrábica, los pedidos a `POST /api/turnos` y `GET /api/turnos/agenda` se multiplican por diez, mientras que el alta de dueños sigue igual. Para absorber el pico hay que levantar, por ejemplo, 4 copias del `vet-system.jar` completo detrás de un balanceador. Cada copia consume la memoria de los cuatro módulos, y las 4 instancias abren conexiones contra la misma base PostgreSQL. Esa base termina siendo el cuello de botella, porque las consultas de agenda compiten con el resto del sistema.

**Cómo lo resuelve la Fase 2.** Turnos pasa a ser un microservicio propio, registrado en Eureka detrás del API Gateway, y se escala de forma independiente (por ejemplo, 4 réplicas de `turnos-service` y 1 de `duenios-service`). Cada servicio escala según su propia demanda.

## 2. Falla total — ¿qué pasa si Veterinarios falla por un bug?

**Problema.** Todos los módulos corren en el mismo proceso de la JVM y comparten el mismo heap, el mismo pool de threads de Tomcat y el mismo pool de conexiones a la base. Un error que tira una excepción en una request queda contenido: el `GlobalExceptionHandler` lo convierte en un 500 y el resto sigue funcionando. Pero un bug que agota un recurso compartido afecta a todo el sistema.

**Ejemplo en la clínica.** Supongamos que se agrega a `VeterinarioService` un reporte que carga en memoria todos los turnos históricos de cada veterinario. Con el crecimiento de la clínica, ese endpoint provoca un `OutOfMemoryError` o deja las conexiones del pool tomadas en consultas lentas. El proceso se cae o se bloquea, y con él dejan de responder `/api/duenios`, `/api/mascotas` y `/api/turnos`. La recepción no puede dar turnos por un bug en un módulo que no tiene nada que ver.

**Cómo lo resuelve la Fase 2.** Cada microservicio corre en su propio proceso o contenedor, con sus propios recursos. Si `veterinarios-service` se cae, el resto sigue funcionando. Además, con Resilience4J (circuit breaker) en el Gateway y en las llamadas OpenFeign, `turnos-service` puede degradarse con un fallback en lugar de quedarse esperando a un servicio caído.

## 3. Base de datos y tecnología únicas — ¿MongoDB para el historial y otra base para los dueños?

**Problema.** El monolito tiene un único `DataSource` y un único modelo de persistencia (JPA sobre PostgreSQL) compartido por todos los módulos. Las entidades están acopladas entre sí por claves foráneas y relaciones JPA (`Turno` referencia a `Mascota` y a `Veterinario`, `Mascota` referencia a `Duenio`). Cambiar la tecnología de persistencia de un solo módulo implica romper esas relaciones y convivir con dos modelos de datos en el mismo código.

**Ejemplo en la clínica.** El historial clínico de una mascota (turnos con observaciones, diagnósticos, estudios adjuntos) es un dato semiestructurado que crece sin límite y encaja mejor en un documento de MongoDB. Los dueños y veterinarios, en cambio, son datos relacionales que conviene mantener en SQL. Hoy `TurnoRepository` es un `JpaRepository` y `Turno` tiene un `@ManyToOne` hacia `Mascota`. Llevar solo los turnos a MongoDB obligaría a reescribir `TurnoService`, eliminar las relaciones JPA y mantener dos configuraciones de persistencia dentro del mismo deploy, con el riesgo de afectar a los otros módulos.

**Cómo lo resuelve la Fase 2.** Con el patrón *database per service*, cada microservicio es dueño de su base y elige la tecnología que necesita: `turnos-service` con MongoDB para el historial, `duenios-service` con una base relacional y Redis como caché de consultas frecuentes. Los servicios dejan de compartir tablas y pasan a referenciarse por ID a través de sus APIs.

## 4. Equipos acoplados — ¿qué pasa si dos equipos trabajan en paralelo?

**Problema.** Un solo repositorio, un solo `pom.xml` y un solo artefacto significan que todos los equipos modifican el mismo código base y comparten el mismo ciclo de release. Los cambios de un equipo pueden romper el trabajo del otro, y la coordinación crece con la cantidad de personas.

**Ejemplo en la clínica.** Un equipo trabaja en Turnos y otro en Mascotas. Si el equipo de Mascotas cambia la entidad `Mascota` (renombra un campo o cambia `MascotaRepository`), rompe la compilación de `TurnoService`, que usa ese repositorio directamente. También puede romper `TurnoMapper`, que mapea `mascota.nombre`. Además, los dos equipos editan los mismos archivos compartidos (`pom.xml`, `GlobalExceptionHandler`, `application.properties`) y generan conflictos de merge. Los dos dependen de que pase la suite completa de `mvn test` para integrar su trabajo, aunque sus cambios no tengan relación entre sí.

**Cómo lo resuelve la Fase 2.** Cada microservicio tiene su propio código, sus propias dependencias y su propio pipeline, a cargo de un equipo. Los equipos se comunican a través de contratos de API (documentados con OpenAPI/Swagger y consumidos con OpenFeign), no compartiendo clases internas. Mientras el contrato no cambie, cada equipo evoluciona su servicio sin coordinar con los demás.

## 5. Deploy monolítico — ¿qué pasa si queremos deployar solo una actualización de Mascotas?

**Problema.** No existe el deploy parcial: cualquier cambio, por mínimo que sea, requiere recompilar, volver a testear y redeployar la aplicación completa. Cada deploy implica reiniciar el proceso entero y, con él, todos los módulos.

**Ejemplo en la clínica.** Se corrige una validación en `MascotaDTO` (por ejemplo, que la raza pase a ser obligatoria). Aunque el cambio es de una línea, hay que generar un nuevo `vet-system.jar` y reiniciarlo. Durante el reinicio, la recepción no puede consultar la agenda ni dar turnos. Si el cambio en Mascotas introduce un error de arranque, la aplicación completa queda fuera de servicio y el rollback también es de todo el sistema. El riesgo de cada deploy es el de toda la aplicación, no el del cambio.

**Cómo lo resuelve la Fase 2.** Cada microservicio se construye y se deploya por separado (contenedores Docker orquestados con Docker Compose). Actualizar `mascotas-service` solo reinicia ese servicio; Turnos, Dueños y Veterinarios siguen atendiendo. El rollback también es solo de ese servicio.

---

## Conclusión

El monolito no está mal diseñado: para una clínica con volumen moderado y un solo equipo, su simplicidad es una ventaja. Los cinco pain points aparecen cuando el sistema crece en carga, en datos o en cantidad de personas trabajando sobre él, porque todos comparten la misma raíz: **un único proceso, una única base de datos y un único ciclo de deploy para módulos con necesidades distintas**. La migración a microservicios de la Fase 2 no se justifica como moda, sino como respuesta concreta a esos límites. El costo también es concreto: comunicación por red, consistencia eventual entre servicios y más infraestructura para operar (Eureka, Config Server, Gateway, Docker). Por eso la migración se hará en forma gradual con el patrón *Strangler Fig*, empezando por reestructurar el código con arquitectura hexagonal en el Sprint 9.
