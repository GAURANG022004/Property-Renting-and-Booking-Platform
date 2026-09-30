package com.gaurang.property_rental.dto.owner;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record AvailabilityWindowResponse(
        Long id,
        Long propertyId,
        String propertyTitle,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate startDate,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate endDate
) { }