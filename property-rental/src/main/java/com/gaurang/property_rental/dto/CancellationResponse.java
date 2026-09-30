package com.gaurang.property_rental.dto;

import java.math.BigDecimal;

public record CancellationResponse(
        Long bookingId,
        String status,
        BigDecimal refundAmount,
        String refundStatus,
        String message
) { }