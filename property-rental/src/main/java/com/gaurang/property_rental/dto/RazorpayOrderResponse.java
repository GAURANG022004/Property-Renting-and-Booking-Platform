package com.gaurang.property_rental.dto;

public record RazorpayOrderResponse(
        Long bookingId,
        String orderId,
        String keyId,
        long amount,
        String currency,
        String propertyTitle
) { }