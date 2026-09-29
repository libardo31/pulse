package com.pulsepass.dto.response;

import com.pulsepass.domain.CategoriaEvento;
import com.pulsepass.domain.EstadoEvento;

import java.time.LocalDateTime;

public record EventResponse(
        Long id,
        String eventCode,
        String name,
        String description,
        CategoriaEvento category,
        EstadoEvento status,
        LocalDateTime eventDate,
        Integer minimumAge,
        String streamingUrl,
        String venueCode
) {
}
