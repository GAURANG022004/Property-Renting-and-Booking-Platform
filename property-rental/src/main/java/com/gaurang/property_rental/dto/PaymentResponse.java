package com.gaurang.property_rental.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record PaymentResponse(
        Long id,
        Long bookingId,
        String propertyTitle,
        double amount,
        String paymentMethod,
        String transactionId,
        String status,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate paymentDate,
        String failureReason
) {
}