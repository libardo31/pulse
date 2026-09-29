package com.pulsepass.service.impl;

import com.pulsepass.domain.EstadoEvento;
import com.pulsepass.domain.Event;
import com.pulsepass.dto.request.ChangeEventStatusRequest;
import com.pulsepass.dto.response.EventResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.EventMapper;
import com.pulsepass.repository.EventRepository;
import com.pulsepass.service.EventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository repository;
    private final EventMapper mapper;

    public EventServiceImpl(EventRepository repository, EventMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public EventResponse findByCode(String eventCode) {
        return repository.findByEventCode(eventCode)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Event not found: " + eventCode));
    }

    @Override
    public List<EventResponse> findByStatus(EstadoEvento status) {
        return repository.findByStatusOrderByEventDateAsc(status)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public EventResponse changeStatus(String eventCode, ChangeEventStatusRequest request) {
        Event event = repository.findByEventCode(eventCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Event not found: " + eventCode));

        validateTransition(event.getStatus(), request.status());
        event.setStatus(request.status());

        return mapper.toResponse(repository.save(event));
    }

    private void validateTransition(EstadoEvento current, EstadoEvento next) {
        boolean valid = switch (current) {
            case DRAFT -> next == EstadoEvento.PUBLISHED || next == EstadoEvento.CANCELLED;
            case PUBLISHED -> next == EstadoEvento.SOLD_OUT
                    || next == EstadoEvento.CANCELLED
                    || next == EstadoEvento.FINISHED;
            case SOLD_OUT -> next == EstadoEvento.FINISHED;
            case CANCELLED, FINISHED -> false;
        };

        if (!valid) {
            throw new BusinessRuleException(
                    "Invalid event status transition: " + current + " -> " + next);
        }
    }
}
