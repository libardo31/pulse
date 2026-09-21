package com.pulsepass.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "artists")
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stage_name", nullable = false, unique = true, length = 150)
    private String stageName;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(nullable = false, length = 100)
    private String genre;

    @Column(nullable = false)
    private boolean active;

    @ManyToMany(mappedBy = "artists")
    private List<Event> events = new ArrayList<>();

    protected Artist() {
    }

    public Artist(String stageName, String country, String genre, boolean active) {
        this.stageName = stageName;
        this.country = country;
        this.genre = genre;
        this.active = active;
    }

    public Long getId() { return id; }
    public String getStageName() { return stageName; }
    public String getCountry() { return country; }
    public String getGenre() { return genre; }
    public boolean isActive() { return active; }
    public List<Event> getEvents() { return events; }

    public void setStageName(String stageName) { this.stageName = stageName; }
    public void setCountry(String country) { this.country = country; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setActive(boolean active) { this.active = active; }
}
