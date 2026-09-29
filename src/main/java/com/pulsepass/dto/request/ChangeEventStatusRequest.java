package com.pulsepass.dto.request;

import com.pulsepass.domain.EstadoEvento;

public record ChangeEventStatusRequest(EstadoEvento status) {
}
