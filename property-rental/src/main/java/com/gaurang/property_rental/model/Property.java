package com.gaurang.property_rental.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "properties")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private double pricePerNight;

    @Column(nullable = false)
    private double rating;

    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'APPROVED'")
    // A property created by an owner is a live listing unless it is explicitly
    // deactivated or moderated later.  This keeps it visible to tenants through
    // the public /properties endpoints immediately after creation.
    private String approvalStatus = "APPROVED";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();

    protected Property() {
    }

    public Property(String title, String description, String location, double pricePerNight, double rating, User owner) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.pricePerNight = pricePerNight;
        this.rating = rating;
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public double getRating() {
        return rating;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public User getOwner() {
        return owner;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void update(String title, String description, String location, double pricePerNight) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.pricePerNight = pricePerNight;
    }

    public void deactivate() {
        this.approvalStatus = "INACTIVE";
    }
}
