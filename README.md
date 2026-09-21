# PulsePass

Proyecto de persistencia para una plataforma de eventos, artistas y entradas.

Desarrollado con Java 21, Spring Boot 4, Spring Data JPA, Hibernate,
PostgreSQL, Flyway y Testcontainers.

## Modelo de datos

El proyecto contiene las siguientes entidades:

- `Venue`
- `Event`
- `Artist`
- `User`
- `UserProfile`
- `Ticket`

Relaciones principales:

- `Venue` 1:N `Event`
- `Event` N:M `Artist`
- `User` 1:1 `UserProfile`
- `User` 1:N `Ticket`
- `Event` 1:N `Ticket`

La relación N:M entre `Event` y `Artist` utiliza la tabla intermedia
`event_artists`.

## Reglas de integridad

La base de datos utiliza restricciones para proteger la información:

- `Venue.code` es UNIQUE.
- `Venue.capacity` debe ser mayor que cero.
- `Event.eventCode` es UNIQUE.
- `Artist.stageName` es UNIQUE.
- `User.username` es UNIQUE.
- `User.email` es UNIQUE.
- `UserProfile.user_id` es UNIQUE para garantizar la relación 1:1.
- `Ticket.ticketCode` es UNIQUE.
- `Ticket.price` debe ser mayor o igual a cero.
- Las relaciones utilizan claves foráneas.

Los enums se almacenan por nombre y no por ordinal.

## Migraciones Flyway

El esquema de la base de datos es administrado mediante Flyway.

### V1__create_schema.sql

Crea las tablas principales:

- `venues`
- `events`
- `artists`
- `event_artists`
- `users`
- `user_profiles`
- `tickets`

También crea claves primarias, claves foráneas, restricciones e índices.

### V2__insert_initial_artists.sql

Inserta los artistas iniciales:

- Solar Beat
- Neon Waves
- Caribbean Sound
- Ocean Drive
- Digital Pulse

### V3__add_streaming_url_to_event.sql

Agrega el campo opcional `streaming_url` a la tabla `events`.

La aplicación utiliza:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate# pulsepass
# pulsepass
