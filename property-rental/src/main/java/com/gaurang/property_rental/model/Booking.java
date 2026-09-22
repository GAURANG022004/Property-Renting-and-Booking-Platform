package com.gaurang.property_rental.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate checkIn;

    @Column(nullable = false)
    private LocalDate checkOut;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id")
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'PENDING'")
    private String status = "PENDING";

    protected Booking() {
    }

    public Booking(LocalDate checkIn, LocalDate checkOut, Property property, User user) {
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.property = property;
        this.user = user;
        this.status = "PENDING";
    }

    public Long getId() {
        return id;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public Property getProperty() {
        return property;
    }

    public User getUser() {
        return user;
    }

    public String getStatus() {
        return status;
    }

    public void cancel() {
        this.status = "CANCELLED";
    }

    public void decide(String status) {
        this.status = status;
    }
}
