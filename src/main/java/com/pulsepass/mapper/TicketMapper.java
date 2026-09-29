package com.pulsepass.mapper;

import com.pulsepass.domain.Ticket;
import com.pulsepass.dto.response.TicketResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "eventCode", source = "event.eventCode")
    TicketResponse toResponse(Ticket ticket);
}
