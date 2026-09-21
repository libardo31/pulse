package com.pulsepass.repository;

import com.pulsepass.domain.Event;
import com.pulsepass.domain.EstadoEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByEventCode(String eventCode);

    List<Event> findByStatusOrderByEventDateAsc(EstadoEvento status);

    List<Event> findByVenueCode(String code);

    @Query("""
            SELECT DISTINCT e
            FROM Event e
            JOIN e.artists a
            WHERE LOWER(a.stageName) = LOWER(:stageName)
            ORDER BY e.eventDate ASC
            """)
    List<Event> findEventsByArtist(@Param("stageName") String stageName);

    @Query("""
            SELECT DISTINCT e
            FROM Event e
            JOIN e.artists a
            JOIN e.venue v
            WHERE LOWER(v.city) = LOWER(:city)
              AND LOWER(a.stageName) = LOWER(:stageName)
            ORDER BY e.eventDate ASC
            """)
    List<Event> findEventsByCityAndArtist(
            @Param("city") String city,
            @Param("stageName") String stageName);

    @Query("""
            SELECT DISTINCT e
            FROM Event e
            JOIN e.artists a
            JOIN e.venue v
            WHERE e.status = com.pulsepass.domain.EstadoEvento.PUBLISHED
              AND e.eventDate > :fromDate
              AND LOWER(v.city) = LOWER(:city)
              AND LOWER(a.stageName) LIKE LOWER(CONCAT('%', :artistText, '%'))
            ORDER BY e.eventDate ASC
            """)
    List<Event> findRecommendedEvents(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("city") String city,
            @Param("artistText") String artistText);
}
