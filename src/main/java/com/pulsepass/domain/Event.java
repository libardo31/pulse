package com.pulsepass.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_code", nullable = false, unique = true, length = 50)
    private String eventCode;

    @Column(nullable = false, length = 180)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaEvento category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoEvento status;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "minimum_age", nullable = false)
    private Integer minimumAge;

    @Column(name = "streaming_url", length = 500)
    private String streamingUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @ManyToMany
    @JoinTable(name = "event_artists",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "artist_id"))
    private Set<Artist> artists = new HashSet<>();

    @OneToMany(mappedBy = "event")
    private List<Ticket> tickets = new ArrayList<>();

    protected Event() {
    }

    public Event(String eventCode, String name, String description,
                 CategoriaEvento category, EstadoEvento status,
                 LocalDateTime eventDate, Integer minimumAge) {
        this.eventCode = eventCode;
        this.name = name;
        this.description = description;
        this.category = category;
        this.status = status;
        this.eventDate = eventDate;
        this.minimumAge = minimumAge;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
        if (venue != null && !venue.getEvents().contains(this)) {
            venue.getEvents().add(this);
        }
    }

    public void agregarArtista(Artist artist) {
        if (artist == null) {
            return;
        }
        if (artists.add(artist)) {
            artist.getEvents().add(this);
        }
    }

    public Long getId() { return id; }
    public String getEventCode() { return eventCode; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public CategoriaEvento getCategory() { return category; }
    public EstadoEvento getStatus() { return status; }
    public LocalDateTime getEventDate() { return eventDate; }
    public Integer getMinimumAge() { return minimumAge; }
    public String getStreamingUrl() { return streamingUrl; }
    public Venue getVenue() { return venue; }
    public Set<Artist> getArtists() { return artists; }
    public List<Ticket> getTickets() { return tickets; }

    public void setEventCode(String eventCode) { this.eventCode = eventCode; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(CategoriaEvento category) { this.category = category; }
    public void setStatus(EstadoEvento status) { this.status = status; }
    public void setEventDate(LocalDateTime eventDate) { this.eventDate = eventDate; }
    public void setMinimumAge(Integer minimumAge) { this.minimumAge = minimumAge; }
    public void setStreamingUrl(String streamingUrl) { this.streamingUrl = streamingUrl; }
}
