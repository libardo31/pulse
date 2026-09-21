package com.pulsepass.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_code", nullable = false, unique = true, length = 60)
    private String ticketCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoTicket type;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTicket status;

    @Column(name = "purchase_date", nullable = false)
    private LocalDateTime purchaseDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    protected Ticket() {
    }

    public Ticket(String ticketCode, TipoTicket type, BigDecimal price,
                  EstadoTicket status, LocalDateTime purchaseDate) {
        this.ticketCode = ticketCode;
        this.type = type;
        this.price = price;
        this.status = status;
        this.purchaseDate = purchaseDate;
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null && !user.getTickets().contains(this)) {
            user.getTickets().add(this);
        }
    }

    public void setEvent(Event event) {
        this.event = event;
        if (event != null && !event.getTickets().contains(this)) {
            event.getTickets().add(this);
        }
    }

    public Long getId() { return id; }
    public String getTicketCode() { return ticketCode; }
    public TipoTicket getType() { return type; }
    public BigDecimal getPrice() { return price; }
    public EstadoTicket getStatus() { return status; }
    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public User getUser() { return user; }
    public Event getEvent() { return event; }

    public void setTicketCode(String ticketCode) { this.ticketCode = ticketCode; }
    public void setType(TipoTicket type) { this.type = type; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setStatus(EstadoTicket status) { this.status = status; }
    public void setPurchaseDate(LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; }
}
