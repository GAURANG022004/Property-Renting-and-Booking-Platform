package com.gaurang.property_rental.repository;

import com.gaurang.property_rental.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findAllByUserIdOrderByPaymentDateDesc(Long userId);
    List<Payment> findAllByBookingId(Long bookingId);
    Payment findByTransactionId(String transactionId);
}