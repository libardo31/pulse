package com.pulsepass.service;

import com.pulsepass.dto.request.CreateTicketRequest;
import com.pulsepass.dto.response.TicketResponse;

public interface TicketService {

    TicketResponse create(CreateTicketRequest request);
}
