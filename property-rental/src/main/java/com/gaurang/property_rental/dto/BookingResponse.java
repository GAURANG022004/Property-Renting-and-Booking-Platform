package com.gaurang.property_rental.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record BookingResponse(
        Long id,
        Long propertyId,
        String propertyTitle,
        String location,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate checkIn,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate checkOut,
        String status,
        double totalAmount,
        int guestCount,
        String bookingType,
        int maxGuests,
        String checkInTime,
        String checkOutTime,
        String houseRules,
        int cancellationFreeHours,
        int refundPercentBeforeDeadline,
        int refundPercentWithinDeadline,
        int refundPercentAfterCheckIn,
        java.math.BigDecimal refundAmount,
        String refundStatus
) {
}
