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

    @Column(length = 50)
    private String transactionId;

    @Column(length = 100)
    private String razorpayOrderId;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(nullable = false)
    private LocalDate paymentDate;

    @Column(length = 500)
    private String failureReason;

    @Column(precision = 12, scale = 2)
    private java.math.BigDecimal refundAmount;

    @Column(length = 100)
    private String razorpayRefundId;

    @Column(length = 20)
    private String refundStatus;

    protected Payment() {
    }

    public Payment(Booking booking, User user, double amount, String paymentMethod, String razorpayOrderId) {
        this.booking = booking;
        this.user = user;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.razorpayOrderId = razorpayOrderId;
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

    public String getRazorpayOrderId() {
        return razorpayOrderId;
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

    public java.math.BigDecimal getRefundAmount() { return refundAmount == null ? java.math.BigDecimal.ZERO : refundAmount; }
    public String getRazorpayRefundId() { return razorpayRefundId; }
    public String getRefundStatus() { return refundStatus; }

    public void markAsCompleted() {
        this.status = "COMPLETED";
    }

    public void markAsCompleted(String transactionId) {
        this.transactionId = transactionId;
        this.status = "COMPLETED";
        this.paymentDate = LocalDate.now();
    }

    public void markAsFailed(String reason) {
        this.status = "FAILED";
        this.failureReason = reason;
    }

    public void markAsRefunded() {
        this.status = "REFUNDED";
    }

    public void recordRefund(java.math.BigDecimal amount, String refundId, String status) {
        this.refundAmount = amount;
        this.razorpayRefundId = refundId;
        this.refundStatus = status;
        this.status = amount.compareTo(java.math.BigDecimal.valueOf(this.amount)) >= 0 ? "REFUNDED" : "PARTIALLY_REFUNDED";
    }

    public void recordRefundFailure(String reason) {
        this.refundStatus = "FAILED";
        this.failureReason = reason;
    }
}