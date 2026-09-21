package com.pulsepass.repository;

import com.pulsepass.domain.EstadoTicket;
import com.pulsepass.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByUserEmailIgnoreCaseAndStatus(String email, EstadoTicket status);

    List<Ticket> findByEventEventCodeAndStatus(String eventCode, EstadoTicket status);

    long countByEventEventCodeAndStatus(String eventCode, EstadoTicket status);

    List<Ticket> findByEventEventDateAfterOrderByEventEventDateAsc(LocalDateTime date);

    @Query("""
            SELECT COUNT(t)
            FROM Ticket t
            JOIN t.event e
            WHERE e.eventCode = :eventCode
              AND t.status = com.pulsepass.domain.EstadoTicket.PAID
            """)
    long contarTicketsPagadosPorEvento(@Param("eventCode") String eventCode);
}
