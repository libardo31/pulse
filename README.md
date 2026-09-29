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
      ddl-auto: validate
## Capa de servicio — continuación del taller

Este proyecto continúa el taller anterior de persistencia de PulsePass. Se conserva
el dominio, las relaciones JPA, las migraciones Flyway, los repositories y las
pruebas de integración existentes.

### Componentes agregados

- DTOs mediante `record`.
- MapStruct para Entity → DTO.
- `ResourceNotFoundException` para recursos inexistentes.
- `BusinessRuleException` para operaciones no permitidas por reglas de negocio.
- `EventService` / `EventServiceImpl`.
- `TicketService` / `TicketServiceImpl`.
- `ArtistService` / `ArtistServiceImpl` como reto adicional de integración.
- Inyección de dependencias mediante constructor.
- `@Transactional(readOnly = true)` para lecturas y `@Transactional` para operaciones de escritura.
- `Optional` con `orElseThrow()`.
- Transformaciones con `stream()`, `map()` y `toList()`.
- Unit tests con JUnit, Mockito y AssertJ.

### Adaptación del laboratorio de capa de servicio

El documento de servicio utiliza el dominio DeepBlue Rescue como ejemplo. En este
proyecto se conserva la misma estructura pedagógica, pero aplicada al dominio
PulsePass existente:

- `RescueCaseService` → `EventService`.
- `TreatmentService` → `TicketService`.
- Reto `AnimalService` → `ArtistService`.
- `RescueCaseResponse` → `EventResponse`.
- `TreatmentResponse` → `TicketResponse`.

Las reglas específicas de DeepBlue se sustituyen por reglas coherentes con el
modelo PulsePass y su PRD. Por ejemplo, un ticket solo puede crearse para un
evento publicado, un usuario debe estar activo y no se permite crear tickets
para eventos ya realizados.

### Pruebas unitarias

Las pruebas de servicio utilizan Mockito para reemplazar repositories y mappers,
por lo que no necesitan PostgreSQL ni Testcontainers. Se cubren, entre otros:

- recurso existente → DTO;
- recurso inexistente → `ResourceNotFoundException`;
- transición válida → `save()`;
- transición inválida → `BusinessRuleException` y nunca `save()`;
- creación válida de ticket → `save()`;
- usuario inactivo → `BusinessRuleException`;
- evento no publicado o ya realizado → `BusinessRuleException`;
- código de ticket duplicado → `BusinessRuleException`.

Para ejecutar todas las pruebas:

```bash
mvn clean test
```

Las pruebas de integración del taller anterior continúan usando PostgreSQL mediante
Testcontainers; las pruebas unitarias nuevas no levantan contenedores.
