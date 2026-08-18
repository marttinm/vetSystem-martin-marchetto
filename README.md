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
