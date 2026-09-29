package com.pulsepass.service.impl;

import com.pulsepass.domain.EstadoEvento;
import com.pulsepass.domain.EstadoTicket;
import com.pulsepass.domain.Event;
import com.pulsepass.domain.Ticket;
import com.pulsepass.domain.User;
import com.pulsepass.dto.request.CreateTicketRequest;
import com.pulsepass.dto.response.TicketResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.TicketMapper;
import com.pulsepass.repository.EventRepository;
import com.pulsepass.repository.TicketRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.service.TicketService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final TicketMapper mapper;

    public TicketServiceImpl(
            TicketRepository ticketRepository,
            UserRepository userRepository,
            EventRepository eventRepository,
            TicketMapper mapper) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public TicketResponse create(CreateTicketRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.userEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + request.userEmail()));

        Event event = eventRepository.findByEventCode(request.eventCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Event not found: " + request.eventCode()));

        validateRequest(request, user, event);

        if (ticketRepository.findByTicketCode(request.ticketCode()).isPresent()) {
            throw new BusinessRuleException(
                    "Ticket code already exists: " + request.ticketCode());
        }

        Ticket ticket = new Ticket(
                request.ticketCode(),
                request.type(),
                request.price(),
                EstadoTicket.RESERVED,
                request.purchaseDate());
        ticket.setUser(user);
        ticket.setEvent(event);

        return mapper.toResponse(ticketRepository.save(ticket));
    }

    private void validateRequest(CreateTicketRequest request, User user, Event event) {
        if (!user.isActive()) {
            throw new BusinessRuleException(
                    "Cannot create a ticket for an inactive user: " + user.getEmail());
        }

        if (event.getStatus() != EstadoEvento.PUBLISHED) {
            throw new BusinessRuleException(
                    "Tickets can only be created for PUBLISHED events: " + event.getEventCode());
        }

        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException(
                    "Cannot create a ticket for an event that has already occurred: "
                            + event.getEventCode());
        }

        if (request.price() == null || request.price().signum() < 0) {
            throw new BusinessRuleException("Ticket price cannot be negative");
        }

        if (request.purchaseDate() == null || request.purchaseDate().isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException("Purchase date cannot be in the future");
        }

        if (request.ticketCode() == null || request.ticketCode().isBlank()) {
            throw new BusinessRuleException("Ticket code is required");
        }

        if (request.type() == null) {
            throw new BusinessRuleException("Ticket type is required");
        }
    }
}
