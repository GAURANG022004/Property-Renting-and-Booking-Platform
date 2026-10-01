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

    @Column(nullable = false, length = 10000)
    private String description;

    @Column(nullable = false)
    private String location;

    @Column(length = 2048)
    private String mapUrl;

    private Integer maxGuests = 4;

    @Column(length = 5)
    private String checkInTime = "15:00";

    @Column(length = 5)
    private String checkOutTime = "11:00";

    @Column(length = 2000)
    private String houseRules = "";

    private Integer cancellationFreeHours = 48;
    private Integer refundPercentBeforeDeadline = 100;
    private Integer refundPercentWithinDeadline = 0;
    private Integer refundPercentAfterCheckIn = 0;

    @Column(nullable = false)
    private double pricePerNight;

    @Column(nullable = false)
    private double rating;

    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'APPROVED'")
    // A property created by an owner is a live listing unless it is explicitly
    // deactivated or moderated later.  This keeps it visible to tenants through
    // the public /properties endpoints immediately after creation.
    private String approvalStatus = "APPROVED";

    @Column(length = 20)
    private String statusBeforeDeactivation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();

    protected Property() {
    }

    public Property(String title, String description, String location, double pricePerNight, User owner) {
        this(title, description, location, pricePerNight, null, 0.0, owner);
    }

    public Property(String title, String description, String location, double pricePerNight, double rating, User owner) {
        this(title, description, location, pricePerNight, null, rating, owner);
    }

    public Property(String title, String description, String location, double pricePerNight, String mapUrl, double rating, User owner) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.pricePerNight = pricePerNight;
        this.mapUrl = mapUrl;
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

    public String getMapUrl() {
        return mapUrl;
    }

    public int getMaxGuests() { return maxGuests == null ? 4 : maxGuests; }
    public String getCheckInTime() { return checkInTime == null ? "15:00" : checkInTime; }
    public String getCheckOutTime() { return checkOutTime == null ? "11:00" : checkOutTime; }
    public String getHouseRules() { return houseRules == null ? "" : houseRules; }
    public int getCancellationFreeHours() { return cancellationFreeHours == null ? 48 : cancellationFreeHours; }
    public int getRefundPercentBeforeDeadline() { return refundPercentBeforeDeadline == null ? 100 : refundPercentBeforeDeadline; }
    public int getRefundPercentWithinDeadline() { return refundPercentWithinDeadline == null ? 0 : refundPercentWithinDeadline; }
    public int getRefundPercentAfterCheckIn() { return refundPercentAfterCheckIn == null ? 0 : refundPercentAfterCheckIn; }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
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
        update(title, description, location, pricePerNight, mapUrl);
    }

    public void update(String title, String description, String location, double pricePerNight, String mapUrl) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.pricePerNight = pricePerNight;
        this.mapUrl = mapUrl;
    }

    public void updateBookingRules(Integer maxGuests, String checkInTime, String checkOutTime, String houseRules,
                                   Integer cancellationFreeHours, Integer refundPercentBeforeDeadline,
                                   Integer refundPercentWithinDeadline, Integer refundPercentAfterCheckIn) {
        this.maxGuests = maxGuests == null ? 4 : maxGuests;
        this.checkInTime = checkInTime == null || checkInTime.isBlank() ? "15:00" : checkInTime.trim();
        this.checkOutTime = checkOutTime == null || checkOutTime.isBlank() ? "11:00" : checkOutTime.trim();
        this.houseRules = houseRules == null ? "" : houseRules.trim();
        this.cancellationFreeHours = cancellationFreeHours == null ? 48 : cancellationFreeHours;
        this.refundPercentBeforeDeadline = refundPercentBeforeDeadline == null ? 100 : refundPercentBeforeDeadline;
        this.refundPercentWithinDeadline = refundPercentWithinDeadline == null ? 0 : refundPercentWithinDeadline;
        this.refundPercentAfterCheckIn = refundPercentAfterCheckIn == null ? 0 : refundPercentAfterCheckIn;
    }

    public void deactivate() {
        if (!"INACTIVE".equals(approvalStatus)) {
            statusBeforeDeactivation = approvalStatus;
        }
        this.approvalStatus = "INACTIVE";
    }

    public void reactivate() {
        this.approvalStatus = statusBeforeDeactivation == null ? "PENDING" : statusBeforeDeactivation;
        this.statusBeforeDeactivation = null;
    }
}
