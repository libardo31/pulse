package com.pulsepass.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @Column(nullable = false)
    private boolean active;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private UserProfile profile;

    @OneToMany(mappedBy = "user")
    private List<Ticket> tickets = new ArrayList<>();

    protected User() {
    }

    public User(String username, String email, boolean active) {
        this.username = username;
        this.email = email;
        this.active = active;
    }

    public void asignarPerfil(UserProfile profile) {
        if (this.profile == profile) {
            return;
        }
        this.profile = profile;
        if (profile != null && profile.getUser() != this) {
            profile.setUser(this);
        }
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public boolean isActive() { return active; }
    public UserProfile getProfile() { return profile; }
    public List<Ticket> getTickets() { return tickets; }

    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setActive(boolean active) { this.active = active; }
}
