package com.pulsepass.service;

import com.pulsepass.domain.CategoriaEvento;
import com.pulsepass.domain.EstadoEvento;
import com.pulsepass.domain.Event;
import com.pulsepass.domain.Venue;
import com.pulsepass.dto.request.ChangeEventStatusRequest;
import com.pulsepass.dto.response.EventResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.EventMapper;
import com.pulsepass.repository.EventRepository;
import com.pulsepass.service.impl.EventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    EventRepository repository;

    @Mock
    EventMapper mapper;

    @InjectMocks
    EventServiceImpl service;

    private Event event;
    private EventResponse response;

    @BeforeEach
    void setUp() {
        Venue venue = new Venue("VEN-TEST", "Test Venue", "Santa Marta", "Carrera 1", 1000, true);
        event = new Event("EVT-TEST", "Test Event", "Description", CategoriaEvento.MUSIC,
                EstadoEvento.DRAFT, LocalDateTime.now().plusDays(10), 18);
        event.setVenue(venue);
        response = new EventResponse(1L, "EVT-TEST", "Test Event", "Description",
                CategoriaEvento.MUSIC, EstadoEvento.PUBLISHED, event.getEventDate(), 18, null, "VEN-TEST");
    }

    @Test
    void eventoExistenteRetornaDto() {
        when(repository.findByEventCode("EVT-TEST")).thenReturn(Optional.of(event));
        when(mapper.toResponse(event)).thenReturn(response);

        EventResponse result = service.findByCode("EVT-TEST");

        assertThat(result).isEqualTo(response);
        verify(repository).findByEventCode("EVT-TEST");
        verify(mapper).toResponse(event);
    }

    @Test
    void eventoInexistenteLanzaResourceNotFoundException() {
        when(repository.findByEventCode("EVT-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByCode("EVT-999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Event not found: EVT-999");

        verify(mapper, never()).toResponse(any());
    }

    @Test
    void findByStatusConvierteLaListaAListaDeDtos() {
        when(repository.findByStatusOrderByEventDateAsc(EstadoEvento.PUBLISHED))
                .thenReturn(List.of(event));
        when(mapper.toResponse(event)).thenReturn(response);

        List<EventResponse> result = service.findByStatus(EstadoEvento.PUBLISHED);

        assertThat(result).containsExactly(response);
        verify(mapper).toResponse(event);
    }

    @Test
    void transicionValidaGuardaElEvento() {
        when(repository.findByEventCode("EVT-TEST")).thenReturn(Optional.of(event));
        when(repository.save(event)).thenReturn(event);
        when(mapper.toResponse(event)).thenReturn(response);

        EventResponse result = service.changeStatus(
                "EVT-TEST", new ChangeEventStatusRequest(EstadoEvento.PUBLISHED));

        assertThat(event.getStatus()).isEqualTo(EstadoEvento.PUBLISHED);
        assertThat(result).isEqualTo(response);
        verify(repository).save(event);
    }

    @Test
    void transicionInvalidaLanzaBusinessRuleExceptionYNuncaGuarda() {
        event.setStatus(EstadoEvento.CANCELLED);
        when(repository.findByEventCode("EVT-TEST")).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> service.changeStatus(
                "EVT-TEST", new ChangeEventStatusRequest(EstadoEvento.PUBLISHED)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Invalid event status transition: CANCELLED -> PUBLISHED");

        verify(repository, never()).save(any(Event.class));
    }
}
