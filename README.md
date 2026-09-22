# VetSystem

Sistema de gestión para la clínica veterinaria "Patitas Felices".
Trabajo práctico de Microservicios y APIs Escalables — Universidad de Palermo, 2026.

## Integrante

- Martin Marchetto

## Stack

- Java 21
- Spring Boot 4.1.0 (Web MVC + Data JPA)
- PostgreSQL 14
- Lombok
- Maven

## Cómo levantarlo

1. Crear la base de datos:

```sql
CREATE DATABASE vet_system;
```

2. Revisar la conexión en `vet-system/src/main/resources/application.properties` y ajustar el puerto y el usuario según tu instalación de Postgres:

```properties
spring.datasource.url=jdbc:postgresql://localhost:3306/vet_system
spring.datasource.username=magic
```

El puerto es 3306 y no el 5432 por defecto de Postgres, porque quedó configurado así desde la migración de MySQL.

3. Levantar la aplicación:

```bash
cd vet-system
./mvnw spring-boot:run
```

Queda escuchando en http://localhost:8080. Las tablas las crea Hibernate al iniciar, con `ddl-auto=update`.

## Endpoints

| Método | Ruta | Descripción |
| --- | --- | --- |
| POST | `/api/duenios` | Registra un dueño |
| GET | `/api/duenios/{id}` | Busca un dueño por id |

```bash
curl -X POST http://localhost:8080/api/duenios \
  -H 'Content-Type: application/json' \
  -d '{"nombre":"Martin","apellido":"Marchetto","dni":"12345678","telefono":1155551234,"email":"martin@test.com"}'
```

## Modelo de dominio

Cuatro entidades:

- **Duenio** (1) —< (N) **Mascota** — un dueño tiene muchas mascotas
- **Turno** —> **Mascota** y **Turno** —> **Veterinario** — cada turno es de una mascota con un veterinario

El diagrama de clases está en `docs/diagrama.png` y el screenshot de las tablas creadas en `docs/tablas.png`.

## Estructura

```
vet-system/src/main/java/com/vetSystem/
├── Entity/       entidades JPA
├── Repository/   interfaces de Spring Data
├── Service/      lógica de negocio
└── Controller/   endpoints REST
```

## Parcial 1 — Decisiones de diseño

### Relación Turno–Medicamento

Elegí una relación `@ManyToMany` unidireccional desde `Turno`, mapeada con la tabla intermedia `turno_medicamentos (turno_id, medicamento_id)`. Es muchos a muchos porque en un turno se pueden recetar varios medicamentos y un mismo medicamento se receta en muchos turnos distintos. La hice unidireccional porque el sistema solo necesita navegar del turno a sus medicamentos; desde `Medicamento` nunca necesito saber en qué turnos se usó, así que agregar la lista inversa era complejidad sin uso. Descarté crear una entidad intermedia (`TurnoMedicamento`) porque la consigna no pide guardar datos propios de la receta como dosis o cantidad, y en ese caso `@ManyToMany` es más simple. Si en el futuro hiciera falta guardar esos datos, habría que migrar a la entidad intermedia. El esquema lo genera Hibernate con `spring.jpa.hibernate.ddl-auto=update`, que crea las tablas y columnas nuevas sin borrar los datos que ya existen; lo elegí porque es un proyecto en desarrollo, pero en producción usaría migraciones versionadas con Flyway o Liquibase, porque `update` no borra columnas viejas ni deja registro de los cambios.

### Validación de stock

El control de stock está en la capa de servicio, en `TurnoService.agregarMedicamento`, y no en el controller, porque es una regla de negocio y el controller solo tiene que recibir el pedido y devolver la respuesta. Primero busco el turno y el medicamento, y si alguno no existe lanzo `ResourceNotFoundException`, que se convierte en un 404. Después verifico que el stock sea mayor a 0; si no lo es, lanzo una `BusinessRuleException`, una excepción nueva que creé para las reglas de negocio que no se cumplen. El `GlobalExceptionHandler` la atrapa y devuelve un 422 con el `ErrorResponse` de siempre, indicando qué medicamento no tiene stock. Si hay stock, descuento una unidad y agrego el medicamento a la lista del turno, todo dentro de un `@Transactional`, así el descuento y la asociación se guardan juntos o no se guarda ninguno. Una limitación que queda es que dos pedidos simultáneos podrían leer el mismo stock; lo resolvería con bloqueo optimista usando `@Version` en la entidad.

### Solapamiento

La detección está en `TurnoService.createTurno`, antes de guardar el turno nuevo. Uso la consulta derivada de Spring Data `findFirstByVeterinarioIdAndFechaAndHora`, que busca si ya existe un turno del mismo veterinario en la misma fecha y a la misma hora exacta. Los tres parámetros que comparo son el id del veterinario, la fecha y la hora que llegan en el request. Antes el repositorio tenía un `existsBy...` que solo devolvía `true` o `false`; lo cambié por un `findFirstBy...` que devuelve un `Optional<Turno>`, porque la consigna pide informar cuál es el turno en conflicto y con un booleano no tenía esa información. Si el `Optional` viene con un turno, lanzo `TurnoSuperpuestoException`, que el handler convierte en un 409, y el mensaje incluye el id, la fecha y la hora del turno que choca. La comparación es por hora exacta, así que no detecta turnos que se pisan parcialmente; para eso cada turno necesitaría una duración.

### Cupo de mascotas

La validación está en `MascotaService.createMascota` y se ejecuta después de confirmar que el dueño existe. Uso la consulta `countByDuenioId`, que Spring Data traduce a un `SELECT COUNT(*)` sobre las mascotas filtradas por el id del dueño. Si el resultado ya es 5 o más, lanzo una `BusinessRuleException` y la API responde 422 con un mensaje que indica el dueño y el máximo permitido. El límite está en la constante `MAX_MASCOTAS_POR_DUENIO` para no dejar un número suelto en el código. Como criterio de "mascota activa" tomé todas las mascotas del dueño que existen en la base, porque el borrado de mascotas es físico: una mascota dada de baja se elimina de la tabla, así que todas las que quedan están activas.

### Decisión más difícil

Lo más difícil fue definir qué significa "mascota activa", porque la consigna lo pide pero la entidad `Mascota` no tiene ningún campo que indique si está activa o no. Evalué dos opciones: agregar un campo booleano `activa` y contar solo las que lo tengan en `true`, o contar todas las mascotas del dueño. Elegí la segunda porque en mi sistema el `DELETE` de mascota es físico, así que no existe ninguna mascota inactiva guardada, y agregar un campo que nada pone en `false` hubiera sido agregar complejidad sin uso. Si más adelante la clínica quisiera conservar el historial de mascotas fallecidas o dadas de baja, pasaría a un borrado lógico con ese campo. Otro problema apareció al probar el CRUD de medicamentos: si intentaba borrar un medicamento que ya estaba recetado en un turno, la clave foránea de `turno_medicamentos` hacía fallar el borrado con un error 500. Lo resolví verificando antes en el servicio si el medicamento está asociado a algún turno, y en ese caso devuelvo un 422 que explica por qué no se puede eliminar.
