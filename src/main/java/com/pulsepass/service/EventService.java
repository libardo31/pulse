package com.pulsepass.service;

import com.pulsepass.domain.EstadoEvento;
import com.pulsepass.dto.request.ChangeEventStatusRequest;
import com.pulsepass.dto.response.EventResponse;

import java.util.List;

public interface EventService {

    EventResponse findByCode(String eventCode);

    List<EventResponse> findByStatus(EstadoEvento status);

    EventResponse changeStatus(String eventCode, ChangeEventStatusRequest request);
}
