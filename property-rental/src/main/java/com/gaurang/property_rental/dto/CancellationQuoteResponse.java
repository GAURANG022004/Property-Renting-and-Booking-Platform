package com.gaurang.property_rental.dto;

import java.math.BigDecimal;

public record CancellationQuoteResponse(
        Long bookingId,
        boolean eligible,
        String cancellationPolicy,
        int refundPercent,
        BigDecimal amountPaid,
        BigDecimal refundAmount,
        String refundStatus
) { }