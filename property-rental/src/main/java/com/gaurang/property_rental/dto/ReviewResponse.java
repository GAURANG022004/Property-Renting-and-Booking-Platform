package com.gaurang.property_rental.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record ReviewResponse(
        Long id,
        Long propertyId,
        String propertyTitle,
        Long bookingId,
        String userName,
        Integer rating,
        String comment,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate reviewDate,
        boolean approved
) {
}