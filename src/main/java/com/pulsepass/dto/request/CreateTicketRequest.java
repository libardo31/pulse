package com.pulsepass.dto.request;

import com.pulsepass.domain.TipoTicket;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateTicketRequest(
        String ticketCode,
        String userEmail,
        String eventCode,
        TipoTicket type,
        BigDecimal price,
        LocalDateTime purchaseDate
) {
}
