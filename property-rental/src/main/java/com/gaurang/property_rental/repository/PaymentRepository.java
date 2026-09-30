package com.gaurang.property_rental.repository;

import com.gaurang.property_rental.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findAllByUserIdOrderByPaymentDateDesc(Long userId);
    List<Payment> findAllByBookingId(Long bookingId);
    List<Payment> findAllByBookingIdAndStatus(Long bookingId, String status);
    Payment findByTransactionId(String transactionId);
    Payment findFirstByBookingIdAndStatusOrderByIdDesc(Long bookingId, String status);
    Payment findByRazorpayOrderId(String razorpayOrderId);
}