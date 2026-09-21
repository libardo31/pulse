# pulsePass

plataforma de evento
## descripción

pulsePass permite gestionar venues eventos, artistas, usuarios, perfiles
y entradas

El proyecto se enfoca en la persistencia, consultas, restricciones de base de
datos y pruebas de integracion.

## modelo de datos

Entidades principales:

- Venue
- Event
- Artist
- User
- UserProfile
- Ticket

relaciones:

- Venue 1:N Event
- Event N:M Artist
- User 1:1 UserProfile
- User 1:N Ticket
- Event 1:N Ticket

La relación Event-Artist utiliza la tabla `event_artists`.

## restricciones

- `Venue.code` es UNIQUE y NOT NULL.
- `Venue.capacity` debe ser mayor que 0.
- `Event.eventCode` es UNIQUE y NOT NULL.
- `Artist.stageName` es UNIQUE y NOT NULL.
- `User.username` y `User.email` son UNIQUE y NOT NULL.
- `UserProfile.user_id` es FK y UNIQUE.
- `Ticket.ticketCode` es UNIQUE y NOT NULL.
- `Ticket.price` debe ser mayor o igual a 0.
- Ticket requiere un User y un Event.
- `event_artists` utiliza una clave primaria compuesta.

Los precios utilizan `BigDecimal` y `NUMERIC`.

## enuneraciones

- EventCategory: MUSIC, SPORTS, TECHNOLOGY, EDUCATION, CULTURE, ENTERTAINMENT.
- EventStatus: DRAFT, PUBLISHED, SOLD_OUT, CANCELLED, FINISHED.
- TicketType: GENERAL, VIP, BACKSTAGE, STUDENT.
- TicketStatus: RESERVED, PAID, CANCELLED, USED.

## flyway

Flyway administra el esquema de la base de datos.

- V1: `V1__create_schema.sql` crea tablas, relaciones y restricciones.
- V2: `V2__insert_initial_artists.sql` inserta los artistas iniciales.
- V3: `V3__add_streaming_url_to_event.sql` agrega `streaming_url`.

Hibernate utiliza `ddl-auto=validate`, por lo que solo valida el esquema.

## query methods

se utilizan Query Methods para las consultas sencillas.

### VenueRepository

- `findByCode`

### EventRepository

- `findByEventCode`
- `findByStatusOrderByEventDateAsc`
- `findByVenueCode`

### ArtistRepository

- `findByStageNameIgnoreCase`

### UserRepository

- `findByEmailIgnoreCase`
- `findByUsername`

### TicketRepository

- `findByUserEmailIgnoreCase`
- `findByUserEmailIgnoreCaseAndStatus`
- `findByEventEventCodeAndStatus`
- `findByEventEventCodeAndEventEventDateAfterOrderByEventEventDateAsc`

## JPQL

Las consultas complejas utilizan JPQL mediante `@Query`.

Se incluyen consultas para eventos por artista, eventos por ciudad y artista,
eventos recomendados y conteo de entradas con estado `PAID`.

No se utilizan consultas SQL nativas.

## Datos de prueba

Venue: `VEN-SMR-01`, Marina Convention Center, Santa Marta, capacidad 5000.

Event: `CMF-2026`, Caribbean Music Fest 2026, categoría MUSIC y estado PUBLISHED.

Artistas: Solar Beat, Neon Waves y Caribbean Sound.

Entradas: Andrea VIP PAID, Carlos GENERAL PAID, Laura GENERAL RESERVED
y Miguel VIP CANCELLED.

## Ejecución

Requisitos:

- Java 21
- Maven
- PostgreSQL
- Docker Desktop

Ejecutar las pruebas con:

```bash
mvn clean test