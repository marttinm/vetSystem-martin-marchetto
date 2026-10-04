# VetSystem

Sistema de gestión para la clínica veterinaria "Patitas Felices".
Universidad de Palermo, 2026. El repositorio se utiliza en dos materias:

- **Microservicios y APIs Escalables**: desarrollo de la API REST (sprints y parcial).
- **DevOps**: Trabajo Práctico Integrador sobre el ciclo de vida y despliegue continuo de la API (contenerización, CI/CD y observabilidad).

## Integrante

- Martin Marchetto

## Stack

- Java 21
- Spring Boot 4.1.0 (Web MVC + Data JPA)
- PostgreSQL 14
- Lombok
- Maven
- Docker y Docker Compose

## Cómo levantarlo

### Con Docker

Requiere Docker Desktop corriendo.

1. Crear el archivo de variables de entorno a partir del ejemplo y definir una contraseña:

```bash
cp .env.example .env
```

2. Levantar la API y la base de datos:

```bash
docker compose up --build
```

La API queda en http://localhost:8080. Para apagar todo, `docker compose down`; para borrar también los datos de la base, `docker compose down -v`.

### Sin Docker

1. Crear la base de datos:

```sql
CREATE DATABASE vet_system;
```

2. La conexión se configura con variables de entorno. Si no se definen, se usan estos valores por defecto:

| Variable | Valor por defecto |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/vet_system` |
| `DB_USERNAME` | `magic` |
| `DB_PASSWORD` | (vacío) |

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

Es una relación `@ManyToMany` unidireccional desde `Turno`, con la tabla intermedia `turno_medicamentos (turno_id, medicamento_id)`. Un turno puede recetar varios medicamentos y un medicamento aparece en muchos turnos. La hice unidireccional porque solo necesito ir del turno a sus medicamentos y desde `Medicamento` nunca consulto en qué turnos se usó.
El esquema lo genera Hibernate con `ddl-auto=update`, que crea tablas y columnas nuevas sin borrar datos. Sirve para desarrollo, pero en producción usaría Flyway o Liquibase, porque `update` no elimina columnas viejas ni deja registro de los cambios.
No usé una entidad intermedia porque no necesito guardar datos de la receta como dosis o cantidad, si hicieran falta, migraría a esa opción.

### Validación de stock

Está en `TurnoService.agregarMedicamento` y no en el controller, porque es una regla de negocio. Busco el turno y el medicamento, si alguno no existe, lanzo `ResourceNotFoundException` 404. Si el stock es 0, lanzo `BusinessRuleException`, y el `GlobalExceptionHandler` la devuelve como 422 indicando qué medicamento no tiene stock.
Si hay stock, descuento una unidad y asocio el medicamento al turno dentro de un `@Transactional`, así se guardan las dos cosas o ninguna.
Una limitación es que dos pedidos simultáneos podrían leer el mismo stock, lo resolvería con bloqueo optimista usando `@Version`.

### Solapamiento

Se valida en `TurnoService.createTurno` antes de guardar. Uso `findFirstByVeterinarioIdAndFechaAndHora`, que compara veterinario, fecha y hora exactas. Antes tenía un `existsBy...`, pero devolvía solo un booleano y la consigna pide informar cuál es el turno en conflicto, así que lo cambié por un `findFirstBy...` que devuelve `Optional<Turno>`. Si viene con valor, lanzo `TurnoSuperpuestoException` 409 con el id, la fecha y la hora del turno que choca. Como comparo la hora exacta, no detecto turnos que se pisan parcialmente, para eso cada turno necesitaría una duración.

### Cupo de mascotas

Está en `MascotaService.createMascota`, después de verificar que el dueño existe. `countByDuenioId` genera un `SELECT COUNT(*)` filtrado por dueño; si da 5 o más, lanzo `BusinessRuleException` 422 con el dueño y el máximo permitido. El límite está en la constante `MAX_MASCOTAS_POR_DUENIO`. Cuento todas las mascotas del dueño como activas (explicado abajo). Si la clínica quisiera guardar el historial de mascotas dadas de baja, pasaría a un borrado lógico con un campo `activa`.

### Decisión más difícil

Definir qué es una "mascota activa", porque la consigna lo pide pero `Mascota` no tiene ningún campo para eso. Se me ocurrieron dos opciones: agregar un booleano `activa` o contar todas las mascotas del dueño. Elegí la segunda porque el `DELETE` de mascota es físico, entonces no hay mascotas inactivas en la base.
El otro problema apareció probando el CRUD de medicamentos, borrar uno que ya estaba recetado rompía por la clave foránea de `turno_medicamentos` y devolvía un 500. Lo resolví chequeando en el servicio si el medicamento está asociado a algún turno, si lo está, devuelvo un 427 explicando por qué no se puede eliminar.
