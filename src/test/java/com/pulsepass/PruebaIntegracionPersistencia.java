package com.pulsepass;

import com.pulsepass.domain.*;
import com.pulsepass.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest
@Transactional
class PruebaIntegracionPersistencia {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine")
            .withDatabaseName("pulsepass_test")
            .withUsername("pulsepass")
            .withPassword("pulsepass");

    @Autowired
    VenueRepository venueRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    ArtistRepository artistRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserProfileRepository userProfileRepository;

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void debeEjecutarMigracionesFlyway() {
        Integer version1 = jdbcTemplate.queryForObject(
                "select count(*) from flyway_schema_history where version = '1'", Integer.class);
        Integer version2 = jdbcTemplate.queryForObject(
                "select count(*) from flyway_schema_history where version = '2'", Integer.class);
        Integer version3 = jdbcTemplate.queryForObject(
                "select count(*) from flyway_schema_history where version = '3'", Integer.class);

        assertThat(version1).isEqualTo(1);
        assertThat(version2).isEqualTo(1);
        assertThat(version3).isEqualTo(1);
    }

    @Test
    void debeUsarMetodosHeredadosDelRepositorio() {
        Venue venue = venueRepository.saveAndFlush(
                new Venue("VEN-REPO-01", "Test Venue", "Santa Marta", "Carrera 1", 1000, true));

        assertThat(venue.getId()).isNotNull();
        assertThat(venueRepository.findById(venue.getId())).isPresent();
        assertThat(venueRepository.existsById(venue.getId())).isTrue();
        assertThat(venueRepository.count()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void debeGuardarRelacionUnoAMuchosEntreVenueYEvent() {
        Venue venue = venueRepository.saveAndFlush(
                new Venue("VEN-1N-01", "Convention Center", "Santa Marta", "Carrera 1", 5000, true));

        Event eventOne = crearEvento("EVT-1N-001", "Event One", EstadoEvento.PUBLISHED);
        Event eventTwo = crearEvento("EVT-1N-002", "Event Two", EstadoEvento.DRAFT);
        venue.agregarEvento(eventOne);
        venue.agregarEvento(eventTwo);
        eventRepository.saveAll(List.of(eventOne, eventTwo));
        eventRepository.flush();

        assertThat(eventRepository.findByVenueCode("VEN-1N-01")).hasSize(2);
        assertThat(eventOne.getVenue()).isSameAs(venue);
        assertThat(eventTwo.getVenue()).isSameAs(venue);
    }

    @Test
    void debeGuardarRelacionMuchosAMuchosEntreEventYArtist() {
        Venue venue = venueRepository.saveAndFlush(
                new Venue("VEN-NM-01", "Music Center", "Santa Marta", "Carrera 2", 5000, true));
        Artist solarBeat = artistRepository.findByStageNameIgnoreCase("Solar Beat").orElseThrow();
        Artist neonWaves = artistRepository.findByStageNameIgnoreCase("Neon Waves").orElseThrow();

        Event event = crearEvento("EVT-NM-001", "Caribbean Music Fest", EstadoEvento.PUBLISHED);
        event.setVenue(venue);
        event.agregarArtista(solarBeat);
        event.agregarArtista(neonWaves);
        eventRepository.saveAndFlush(event);

        Event found = eventRepository.findByEventCode("EVT-NM-001").orElseThrow();
        assertThat(found.getArtists()).extracting(Artist::getStageName)
                .containsExactlyInAnyOrder("Solar Beat", "Neon Waves");

        Integer associations = jdbcTemplate.queryForObject(
                "select count(*) from event_artists where event_id = ?", Integer.class, event.getId());
        assertThat(associations).isEqualTo(2);
    }

    @Test
    void debeGuardarRelacionUnoAUnoEntreUserYUserProfile() {
        User user = userRepository.saveAndFlush(
                new User("andrea01", "andrea@pulsepass.test", true));
        UserProfile profile = new UserProfile(
                "Andrea", "Gomez", "3001112222", "Santa Marta", LocalDate.of(2004, 5, 10));
        user.asignarPerfil(profile);
        userProfileRepository.saveAndFlush(profile);

        User found = userRepository.findByUsernameIgnoreCase("ANDREA01").orElseThrow();
        assertThat(found.getProfile().getFirstName()).isEqualTo("Andrea");
        assertThat(profile.getUser().getUsername()).isEqualTo("andrea01");
    }

    @Test
    void debeGuardarTicketRelacionadoConUsuarioYEvento() {
        Venue venue = venueRepository.saveAndFlush(
                new Venue("VEN-TKT-01", "Ticket Venue", "Santa Marta", "Carrera 3", 5000, true));
        Event event = crearEvento("EVT-TKT-001", "Ticket Event", EstadoEvento.PUBLISHED);
        event.setVenue(venue);
        eventRepository.saveAndFlush(event);

        User user = userRepository.saveAndFlush(
                new User("carlos01", "carlos@pulsepass.test", true));
        Ticket ticket = new Ticket(
                "TCK-0001", TipoTicket.VIP, new BigDecimal("250000.00"),
                EstadoTicket.PAID, LocalDateTime.of(2026, 9, 1, 10, 0));
        ticket.setUser(user);
        ticket.setEvent(event);
        ticketRepository.saveAndFlush(ticket);

        Ticket found = ticketRepository.findById(ticket.getId()).orElseThrow();
        assertThat(found.getUser().getEmail()).isEqualTo("carlos@pulsepass.test");
        assertThat(found.getEvent().getEventCode()).isEqualTo("EVT-TKT-001");
    }

    @Test
    void debeBuscarEventosPublicadosOrdenadosPorFecha() {
        Venue venue = venueRepository.saveAndFlush(
                new Venue("VEN-QM-01", "Query Venue", "Santa Marta", "Carrera 4", 5000, true));

        Event later = crearEvento("EVT-QM-002", "Later Event", EstadoEvento.PUBLISHED);
        later.setEventDate(LocalDateTime.of(2026, 12, 20, 20, 0));
        Event earlier = crearEvento("EVT-QM-001", "Earlier Event", EstadoEvento.PUBLISHED);
        earlier.setEventDate(LocalDateTime.of(2026, 10, 20, 20, 0));
        Event draft = crearEvento("EVT-QM-003", "Draft Event", EstadoEvento.DRAFT);
        draft.setEventDate(LocalDateTime.of(2026, 9, 20, 20, 0));

        venue.agregarEvento(later);
        venue.agregarEvento(earlier);
        venue.agregarEvento(draft);
        eventRepository.saveAll(List.of(later, earlier, draft));
        eventRepository.flush();

        assertThat(eventRepository.findByStatusOrderByEventDateAsc(EstadoEvento.PUBLISHED))
                .extracting(Event::getEventCode)
                .containsExactly("EVT-QM-001", "EVT-QM-002");
    }

    @Test
    void debeBuscarUsuarioPorEmailIgnorandoMayusculas() {
        userRepository.saveAndFlush(
                new User("laura01", "Laura@PulsePass.Test", true));

        assertThat(userRepository.findByEmailIgnoreCase("laura@pulsepass.test"))
                .isPresent()
                .get()
                .extracting(User::getUsername)
                .isEqualTo("laura01");
    }

    @Test
    void debeBuscarTicketsDeUsuarioPorEmailYEstado() {
        Venue venue = venueRepository.saveAndFlush(
                new Venue("VEN-USR-01", "User Venue", "Santa Marta", "Carrera 5", 5000, true));
        Event event = crearEvento("EVT-USR-001", "User Event", EstadoEvento.PUBLISHED);
        event.setVenue(venue);
        eventRepository.saveAndFlush(event);
        User user = userRepository.saveAndFlush(
                new User("miguel01", "miguel@pulsepass.test", true));

        Ticket paid = new Ticket("TCK-USR-001", TipoTicket.GENERAL, new BigDecimal("120000"),
                EstadoTicket.PAID, LocalDateTime.now());
        paid.setUser(user);
        paid.setEvent(event);
        Ticket reserved = new Ticket("TCK-USR-002", TipoTicket.GENERAL, new BigDecimal("120000"),
                EstadoTicket.RESERVED, LocalDateTime.now());
        reserved.setUser(user);
        reserved.setEvent(event);
        ticketRepository.saveAll(List.of(paid, reserved));
        ticketRepository.flush();

        assertThat(ticketRepository.findByUserEmailIgnoreCaseAndStatus(
                "MIGUEL@PULSEPASS.TEST", EstadoTicket.PAID))
                .extracting(Ticket::getTicketCode)
                .containsExactly("TCK-USR-001");
    }

    @Test
    void debeBuscarEventosPorArtistaConJpql() {
        Venue venue = venueRepository.saveAndFlush(
                new Venue("VEN-ART-01", "Artist Venue", "Santa Marta", "Carrera 6", 5000, true));
        Artist solarBeat = artistRepository.findByStageNameIgnoreCase("Solar Beat").orElseThrow();
        Event eventOne = crearEvento("EVT-ART-001", "Solar One", EstadoEvento.PUBLISHED);
        Event eventTwo = crearEvento("EVT-ART-002", "Solar Two", EstadoEvento.PUBLISHED);
        eventOne.setVenue(venue);
        eventTwo.setVenue(venue);
        eventOne.agregarArtista(solarBeat);
        eventTwo.agregarArtista(solarBeat);
        eventRepository.saveAll(List.of(eventOne, eventTwo));
        eventRepository.flush();

        assertThat(eventRepository.findEventsByArtist("solar beat"))
                .extracting(Event::getEventCode)
                .containsExactly("EVT-ART-001", "EVT-ART-002");
    }

    @Test
    void debeBuscarEventosPorCiudadYArtistaConJpql() {
        Venue santaMarta = venueRepository.saveAndFlush(
                new Venue("VEN-CITY-01", "Santa Marta Venue", "Santa Marta", "Carrera 7", 5000, true));
        Venue bogota = venueRepository.saveAndFlush(
                new Venue("VEN-CITY-02", "Bogota Venue", "Bogota", "Carrera 8", 5000, true));
        Artist artist = artistRepository.findByStageNameIgnoreCase("Neon Waves").orElseThrow();

        Event santa = crearEvento("EVT-CITY-001", "Santa Event", EstadoEvento.PUBLISHED);
        santa.setVenue(santaMarta);
        santa.agregarArtista(artist);
        Event otherCity = crearEvento("EVT-CITY-002", "Bogota Event", EstadoEvento.PUBLISHED);
        otherCity.setVenue(bogota);
        otherCity.agregarArtista(artist);
        eventRepository.saveAll(List.of(santa, otherCity));
        eventRepository.flush();

        assertThat(eventRepository.findEventsByCityAndArtist("santa marta", "NEON WAVES"))
                .extracting(Event::getEventCode)
                .containsExactly("EVT-CITY-001");
    }

    @Test
    void debeBuscarEventosRecomendadosConJpql() {
        Venue venue = venueRepository.saveAndFlush(
                new Venue("VEN-REC-01", "Recommendation Venue", "Santa Marta", "Carrera 9", 5000, true));
        Artist artist = artistRepository.findByStageNameIgnoreCase("Solar Beat").orElseThrow();

        Event recommended = crearEvento("EVT-REC-001", "Recommended", EstadoEvento.PUBLISHED);
        recommended.setEventDate(LocalDateTime.of(2026, 12, 10, 20, 0));
        recommended.setVenue(venue);
        recommended.agregarArtista(artist);

        Event cancelled = crearEvento("EVT-REC-002", "Cancelled", EstadoEvento.CANCELLED);
        cancelled.setEventDate(LocalDateTime.of(2026, 12, 11, 20, 0));
        cancelled.setVenue(venue);
        cancelled.agregarArtista(artist);

        eventRepository.saveAll(List.of(recommended, cancelled));
        eventRepository.flush();

        assertThat(eventRepository.findRecommendedEvents(
                LocalDateTime.of(2026, 11, 1, 0, 0), "Santa Marta", "solar"))
                .extracting(Event::getEventCode)
                .containsExactly("EVT-REC-001");
    }

    @Test
    void debeBuscarTicketsPagadosPorEventoYContarlos() {
        Venue venue = venueRepository.saveAndFlush(
                new Venue("VEN-SALE-01", "Sales Venue", "Santa Marta", "Carrera 10", 5000, true));
        Event event = crearEvento("EVT-SALE-001", "Sales Event", EstadoEvento.PUBLISHED);
        event.setVenue(venue);
        eventRepository.saveAndFlush(event);
        User user = userRepository.saveAndFlush(
                new User("salesuser", "sales@pulsepass.test", true));

        Ticket paidOne = crearTicket("TCK-SALE-001", EstadoTicket.PAID, user, event);
        Ticket paidTwo = crearTicket("TCK-SALE-002", EstadoTicket.PAID, user, event);
        Ticket reserved = crearTicket("TCK-SALE-003", EstadoTicket.RESERVED, user, event);
        ticketRepository.saveAll(List.of(paidOne, paidTwo, reserved));
        ticketRepository.flush();

        assertThat(ticketRepository.findByEventEventCodeAndStatus("EVT-SALE-001", EstadoTicket.PAID))
                .hasSize(2);
        assertThat(ticketRepository.countByEventEventCodeAndStatus("EVT-SALE-001", EstadoTicket.PAID))
                .isEqualTo(2);
        assertThat(ticketRepository.contarTicketsPagadosPorEvento("EVT-SALE-001"))
                .isEqualTo(2);
    }

    @Test
    void debeBuscarTicketsDeEventosFuturosOrdenados() {
        Venue venue = venueRepository.saveAndFlush(
                new Venue("VEN-FUT-01", "Future Venue", "Santa Marta", "Carrera 11", 5000, true));
        Event first = crearEvento("EVT-FUT-001", "Future One", EstadoEvento.PUBLISHED);
        first.setEventDate(LocalDateTime.of(2027, 1, 10, 20, 0));
        first.setVenue(venue);
        Event second = crearEvento("EVT-FUT-002", "Future Two", EstadoEvento.PUBLISHED);
        second.setEventDate(LocalDateTime.of(2027, 2, 10, 20, 0));
        second.setVenue(venue);
        eventRepository.saveAll(List.of(first, second));
        eventRepository.flush();

        User user = userRepository.saveAndFlush(
                new User("futureuser", "future@pulsepass.test", true));
        ticketRepository.saveAll(List.of(
                crearTicket("TCK-FUT-001", EstadoTicket.PAID, user, second),
                crearTicket("TCK-FUT-002", EstadoTicket.PAID, user, first)));
        ticketRepository.flush();

        assertThat(ticketRepository.findByEventEventDateAfterOrderByEventEventDateAsc(
                LocalDateTime.of(2026, 12, 1, 0, 0)))
                .extracting(ticket -> ticket.getEvent().getEventCode())
                .containsExactly("EVT-FUT-001", "EVT-FUT-002");
    }

    @Test
    void debeRechazarCodigoDeVenueDuplicado() {
        venueRepository.saveAndFlush(
                new Venue("VEN-DUP-01", "First Venue", "Santa Marta", "Carrera 12", 1000, true));

        assertThatThrownBy(() -> venueRepository.saveAndFlush(
                new Venue("VEN-DUP-01", "Second Venue", "Santa Marta", "Carrera 13", 1000, true)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void debeRechazarCodigoDeTicketDuplicado() {
        Venue venue = venueRepository.saveAndFlush(
                new Venue("VEN-TDUP-01", "Ticket Venue", "Santa Marta", "Carrera 14", 1000, true));
        Event event = crearEvento("EVT-TDUP-001", "Ticket Duplicate", EstadoEvento.PUBLISHED);
        event.setVenue(venue);
        eventRepository.saveAndFlush(event);
        User user = userRepository.saveAndFlush(
                new User("dupuser", "dup@pulsepass.test", true));

        ticketRepository.saveAndFlush(crearTicket("TCK-DUP-001", EstadoTicket.PAID, user, event));

        assertThatThrownBy(() -> ticketRepository.saveAndFlush(
                crearTicket("TCK-DUP-001", EstadoTicket.PAID, user, event)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void debeRechazarPerfilDuplicadoParaElMismoUsuario() {
        User user = userRepository.saveAndFlush(
                new User("profiledup", "profiledup@pulsepass.test", true));
        UserProfile first = new UserProfile("Ana", "Lopez", null, "Santa Marta", null);
        first.setUser(user);
        userProfileRepository.saveAndFlush(first);

        assertThatThrownBy(() -> jdbcTemplate.update(
                "insert into user_profiles (first_name, last_name, phone, city, birth_date, user_id) values (?, ?, ?, ?, ?, ?)",
                "Otra", "Persona", null, "Santa Marta", null, user.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void debeRechazarCapacidadNoPositiva() {
        assertThatThrownBy(() -> jdbcTemplate.update(
                "insert into venues (code, name, city, address, capacity, active) values (?, ?, ?, ?, ?, ?)",
                "VEN-CHECK-01", "Invalid Venue", "Santa Marta", "Carrera 15", 0, true))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void debeRechazarPrecioNegativo() {
        Venue venue = venueRepository.saveAndFlush(
                new Venue("VEN-CHECK-02", "Price Venue", "Santa Marta", "Carrera 16", 1000, true));
        Event event = crearEvento("EVT-CHECK-01", "Price Event", EstadoEvento.PUBLISHED);
        event.setVenue(venue);
        eventRepository.saveAndFlush(event);
        User user = userRepository.saveAndFlush(
                new User("checkuser", "check@pulsepass.test", true));

        assertThatThrownBy(() -> jdbcTemplate.update(
                "insert into tickets (ticket_code, type, price, status, purchase_date, user_id, event_id) values (?, ?, ?, ?, ?, ?, ?)",
                "TCK-CHECK-01", "GENERAL", new BigDecimal("-1.00"), "PAID", LocalDateTime.now(), user.getId(), event.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void debeRechazarLlaveForaneaInexistente() {
        assertThatThrownBy(() -> jdbcTemplate.update(
                "insert into tickets (ticket_code, type, price, status, purchase_date, user_id, event_id) values (?, ?, ?, ?, ?, ?, ?)",
                "TCK-FK-01", "GENERAL", new BigDecimal("100.00"), "PAID", LocalDateTime.now(), 999999999L, 999999999L))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void debeEjecutarEscenarioIntegrado() {
        Venue venue = new Venue("VEN-INT-01", "Marina Convention Center", "Santa Marta",
                "Carrera 1 #1-1", 5000, true);
        venueRepository.saveAndFlush(venue);

        Event event = new Event("CMF-2026", "Caribbean Music Fest 2026",
                "Festival de musica del Caribe", CategoriaEvento.MUSIC, EstadoEvento.PUBLISHED,
                LocalDateTime.of(2026, 12, 15, 20, 0), 18);
        event.setVenue(venue);
        Artist solarBeat = artistRepository.findByStageNameIgnoreCase("Solar Beat").orElseThrow();
        Artist neonWaves = artistRepository.findByStageNameIgnoreCase("Neon Waves").orElseThrow();
        Artist caribbeanSound = artistRepository.findByStageNameIgnoreCase("Caribbean Sound").orElseThrow();
        event.agregarArtista(solarBeat);
        event.agregarArtista(neonWaves);
        event.agregarArtista(caribbeanSound);
        eventRepository.saveAndFlush(event);

        User andrea = userRepository.saveAndFlush(new User("andrea", "andrea@pulsepass.test", true));
        UserProfile profile = new UserProfile("Andrea", "Gomez", "3001112222", "Santa Marta", LocalDate.of(2003, 2, 20));
        andrea.asignarPerfil(profile);
        userProfileRepository.saveAndFlush(profile);

        User carlos = userRepository.saveAndFlush(new User("carlos", "carlos@pulsepass.test", true));
        User laura = userRepository.saveAndFlush(new User("laura", "laura@pulsepass.test", true));
        User miguel = userRepository.saveAndFlush(new User("miguel", "miguel@pulsepass.test", true));

        ticketRepository.saveAll(List.of(
                crearTicket("TCK-INT-001", EstadoTicket.PAID, andrea, event, TipoTicket.VIP, "250000"),
                crearTicket("TCK-INT-002", EstadoTicket.PAID, carlos, event, TipoTicket.GENERAL, "120000"),
                crearTicket("TCK-INT-003", EstadoTicket.RESERVED, laura, event, TipoTicket.GENERAL, "120000"),
                crearTicket("TCK-INT-004", EstadoTicket.CANCELLED, miguel, event, TipoTicket.VIP, "250000")));
        ticketRepository.flush();

        assertThat(eventRepository.findByEventCode("CMF-2026")).isPresent();
        assertThat(eventRepository.findEventsByArtist("Solar Beat")).hasSize(1);
        assertThat(ticketRepository.findByEventEventCodeAndStatus("CMF-2026", EstadoTicket.PAID)).hasSize(2);
        assertThat(ticketRepository.contarTicketsPagadosPorEvento("CMF-2026")).isEqualTo(2);
    }

    private Event crearEvento(String eventCode, String name, EstadoEvento status) {
        return new Event(eventCode, name, "Description", CategoriaEvento.MUSIC, status,
                LocalDateTime.of(2026, 12, 1, 20, 0), 0);
    }

    private Ticket crearTicket(String code, EstadoTicket status, User user, Event event) {
        return crearTicket(code, status, user, event, TipoTicket.GENERAL, "100000");
    }

    private Ticket crearTicket(String code, EstadoTicket status, User user, Event event,
                               TipoTicket type, String price) {
        Ticket ticket = new Ticket(code, type, new BigDecimal(price), status, LocalDateTime.now());
        ticket.setUser(user);
        ticket.setEvent(event);
        return ticket;
    }
}
