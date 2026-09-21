package com.pulsepass.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "venues")
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 200)
    private String address;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private boolean active;

    @OneToMany(mappedBy = "venue")
    private List<Event> events = new ArrayList<>();

    protected Venue() {
    }

    public Venue(String code, String name, String city, String address, Integer capacity, boolean active) {
        this.code = code;
        this.name = name;
        this.city = city;
        this.address = address;
        this.capacity = capacity;
        this.active = active;
    }

    public void agregarEvento(Event event) {
        if (event == null) {
            return;
        }
        if (!events.contains(event)) {
            events.add(event);
        }
        if (event.getVenue() != this) {
            event.setVenue(this);
        }
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getAddress() { return address; }
    public Integer getCapacity() { return capacity; }
    public boolean isActive() { return active; }
    public List<Event> getEvents() { return events; }

    public void setCode(String code) { this.code = code; }
    public void setName(String name) { this.name = name; }
    public void setCity(String city) { this.city = city; }
    public void setAddress(String address) { this.address = address; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public void setActive(boolean active) { this.active = active; }
}
