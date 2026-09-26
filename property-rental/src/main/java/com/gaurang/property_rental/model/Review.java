package com.gaurang.property_rental.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false, length = 2000)
    private String comment;

    @Column(nullable = false)
    private LocalDate reviewDate;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean approved = true;

    protected Review() {
    }

    public Review(Property property, User user, Booking booking, int rating, String comment) {
        this.property = property;
        this.user = user;
        this.booking = booking;
        this.rating = rating;
        this.comment = comment;
        this.reviewDate = LocalDate.now();
        this.approved = true;
    }

    public Long getId() {
        return id;
    }

    public Property getProperty() {
        return property;
    }

    public User getUser() {
        return user;
    }

    public Booking getBooking() {
        return booking;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public LocalDate getReviewDate() {
        return reviewDate;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }
}