package com.gaurang.property_rental.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.time.LocalDate;

public record BookingCreateRequest(
        @NotNull Long propertyId,
        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        @FutureOrPresent LocalDate checkIn,
        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        @FutureOrPresent LocalDate checkOut,
        @Min(1) @Max(50) Integer guestCount
) {
}

