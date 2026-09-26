package com.gaurang.property_rental.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false, length = 20)
    private String paymentMethod;

    @Column(nullable = false, length = 50)
    private String transactionId;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(nullable = false)
    private LocalDate paymentDate;

    @Column(length = 500)
    private String failureReason;

    protected Payment() {
    }

    public Payment(Booking booking, User user, double amount, String paymentMethod, String transactionId) {
        this.booking = booking;
        this.user = user;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
        this.paymentDate = LocalDate.now();
        this.status = "PENDING";
    }

    public Long getId() {
        return id;
    }

    public Booking getBooking() {
        return booking;
    }

    public User getUser() {
        return user;
    }

    public double getAmount() {
        return amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getStatus() {
        return status;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void markAsCompleted() {
        this.status = "COMPLETED";
    }

    public void markAsFailed(String reason) {
        this.status = "FAILED";
        this.failureReason = reason;
    }

    public void markAsRefunded() {
        this.status = "REFUNDED";
    }
}