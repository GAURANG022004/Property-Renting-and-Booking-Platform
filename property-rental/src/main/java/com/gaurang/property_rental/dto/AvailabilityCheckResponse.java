package com.gaurang.property_rental.dto;

import java.math.BigDecimal;

public record AvailabilityCheckResponse(
        boolean available,
        boolean instantBooking,
        BigDecimal totalAmount,
        int guestCount,
        int maxGuests,
        String message
) { }