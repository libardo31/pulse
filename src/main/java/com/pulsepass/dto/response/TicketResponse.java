package com.pulsepass.dto.response;

import com.pulsepass.domain.EstadoTicket;
import com.pulsepass.domain.TipoTicket;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TicketResponse(
        Long id,
        String ticketCode,
        TipoTicket type,
        BigDecimal price,
        EstadoTicket status,
        LocalDateTime purchaseDate,
        String username,
        String eventCode
) {
}
