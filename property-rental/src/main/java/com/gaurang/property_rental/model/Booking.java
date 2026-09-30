package com.gaurang.property_rental.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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

    @Column(precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, columnDefinition = "integer default 1")
    private int guestCount = 1;

    @Column(length = 12, columnDefinition = "varchar(12) default 'REQUEST'")
    private String bookingType = "REQUEST";

    @Column(precision = 12, scale = 2)
    private BigDecimal refundAmount = BigDecimal.ZERO;

    @Column(length = 20)
    private String refundStatus;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean cancelledAfterCheckIn;

    protected Booking() {
    }

    public Booking(LocalDate checkIn, LocalDate checkOut, Property property, User user) {
        this(checkIn, checkOut, property, user, 1, false);
    }

    public Booking(LocalDate checkIn, LocalDate checkOut, Property property, User user, int guestCount, boolean instantBooking) {
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.property = property;
        this.user = user;
        this.guestCount = guestCount;
        this.bookingType = instantBooking ? "INSTANT" : "REQUEST";
        this.status = instantBooking ? "PAYMENT_PENDING" : "PENDING";
        this.totalAmount = BigDecimal.valueOf(property.getPricePerNight())
                .multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(checkIn, checkOut)))
                .setScale(2, java.math.RoundingMode.HALF_UP);
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

    public int getGuestCount() { return guestCount; }
    public String getBookingType() { return bookingType == null ? "REQUEST" : bookingType; }
    public BigDecimal getRefundAmount() { return refundAmount == null ? BigDecimal.ZERO : refundAmount; }
    public String getRefundStatus() { return refundStatus; }
    public boolean isCancelledAfterCheckIn() { return cancelledAfterCheckIn; }

    public void recordRefund(BigDecimal amount, String status) {
        this.refundAmount = amount == null ? BigDecimal.ZERO : amount;
        this.refundStatus = status;
    }

    public BigDecimal getTotalAmount() {
        if (totalAmount != null) return totalAmount;
        return BigDecimal.valueOf(property.getPricePerNight())
                .multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(checkIn, checkOut)))
            .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public void cancel() {
        cancelledAfterCheckIn = "ACTIVE".equals(status);
        this.status = "CANCELLED";
    }

    public void accept() {
        this.status = "PAYMENT_PENDING";
    }

    public void reject() {
        this.status = "REJECTED";
    }

    public void confirmPayment() {
        this.status = "CONFIRMED";
    }

    public void checkIn() {
        this.status = "ACTIVE";
    }

    public void complete() {
        this.status = "COMPLETED";
    }
}
