package com.pulsepass.service;

import com.pulsepass.domain.*;
import com.pulsepass.dto.request.CreateTicketRequest;
import com.pulsepass.dto.response.TicketResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.TicketMapper;
import com.pulsepass.repository.EventRepository;
import com.pulsepass.repository.TicketRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.service.impl.TicketServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock
    TicketRepository ticketRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    EventRepository eventRepository;

    @Mock
    TicketMapper mapper;

    @InjectMocks
    TicketServiceImpl service;

    @Test
    void ticketValidoGuardaComoReserved() {
        User user = new User("andrea", "andrea@test.com", true);
        Event event = event(EstadoEvento.PUBLISHED);
        CreateTicketRequest request = request();
        Ticket saved = new Ticket("TCK-001", TipoTicket.VIP, new BigDecimal("250.00"),
                EstadoTicket.RESERVED, request.purchaseDate());
        TicketResponse response = new TicketResponse(1L, "TCK-001", TipoTicket.VIP,
                new BigDecimal("250.00"), EstadoTicket.RESERVED, request.purchaseDate(),
                "andrea", "EVT-001");

        when(userRepository.findByEmailIgnoreCase("andrea@test.com")).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode("EVT-001")).thenReturn(Optional.of(event));
        when(ticketRepository.findByTicketCode("TCK-001")).thenReturn(Optional.empty());
        when(ticketRepository.save(any(Ticket.class))).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        TicketResponse result = service.create(request);

        assertThat(result).isEqualTo(response);
        verify(ticketRepository).save(argThat(ticket -> ticket.getStatus() == EstadoTicket.RESERVED
                && ticket.getUser() == user
                && ticket.getEvent() == event));
    }

    @Test
    void usuarioInexistenteLanzaResourceNotFoundException() {
        when(userRepository.findByEmailIgnoreCase("missing@test.com")).thenReturn(Optional.empty());
        CreateTicketRequest request = new CreateTicketRequest("TCK-002", "missing@test.com", "EVT-001",
                TipoTicket.GENERAL, new BigDecimal("100"), LocalDateTime.now());

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found: missing@test.com");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void usuarioInactivoLanzaBusinessRuleYNuncaGuarda() {
        User user = new User("inactive", "inactive@test.com", false);
        Event event = event(EstadoEvento.PUBLISHED);
        when(userRepository.findByEmailIgnoreCase("inactive@test.com")).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode("EVT-001")).thenReturn(Optional.of(event));

        CreateTicketRequest request = new CreateTicketRequest("TCK-003", "inactive@test.com", "EVT-001",
                TipoTicket.GENERAL, new BigDecimal("100"), LocalDateTime.now());

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("inactive user");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void eventoNoPublicadoImpideCrearTicket() {
        User user = new User("user", "user@test.com", true);
        Event event = event(EstadoEvento.CANCELLED);
        when(userRepository.findByEmailIgnoreCase("user@test.com")).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode("EVT-001")).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> service.create(request()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("PUBLISHED");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void codigoDuplicadoImpideCrearTicket() {
        User user = new User("user", "user@test.com", true);
        Event event = event(EstadoEvento.PUBLISHED);
        when(userRepository.findByEmailIgnoreCase("user@test.com")).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode("EVT-001")).thenReturn(Optional.of(event));
        when(ticketRepository.findByTicketCode("TCK-001")).thenReturn(Optional.of(
                new Ticket("TCK-001", TipoTicket.GENERAL, BigDecimal.TEN, EstadoTicket.PAID, LocalDateTime.now())));

        assertThatThrownBy(() -> service.create(request()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Ticket code already exists: TCK-001");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void eventoYaRealizadoImpideCrearTicket() {
        User user = new User("user", "user@test.com", true);
        Event event = event(EstadoEvento.PUBLISHED);
        event.setEventDate(LocalDateTime.now().minusDays(1));
        when(userRepository.findByEmailIgnoreCase("user@test.com")).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode("EVT-001")).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> service.create(request()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already occurred");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void precioNegativoImpideCrearTicket() {
        User user = new User("user", "user@test.com", true);
        Event event = event(EstadoEvento.PUBLISHED);
        when(userRepository.findByEmailIgnoreCase("user@test.com")).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode("EVT-001")).thenReturn(Optional.of(event));

        CreateTicketRequest request = new CreateTicketRequest("TCK-001", "user@test.com", "EVT-001",
                TipoTicket.GENERAL, new BigDecimal("-1.00"), LocalDateTime.now());

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Ticket price cannot be negative");

        verify(ticketRepository, never()).save(any());
    }

    private CreateTicketRequest request() {
        return new CreateTicketRequest("TCK-001", "user@test.com", "EVT-001",
                TipoTicket.VIP, new BigDecimal("250.00"), LocalDateTime.now().minusMinutes(1));
    }

    private Event event(EstadoEvento status) {
        return new Event("EVT-001", "Event", "Description", CategoriaEvento.MUSIC, status,
                LocalDateTime.now().plusDays(10), 18);
    }
}
