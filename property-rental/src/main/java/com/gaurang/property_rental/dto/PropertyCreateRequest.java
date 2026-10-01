package com.gaurang.property_rental.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PropertyCreateRequest(
        @NotBlank String title,
        @NotBlank @Size(max = 10000) String description,
        @NotBlank String location,
        @NotNull @DecimalMin("0.0") double pricePerNight,
        @Size(max = 2048) @Pattern(regexp = "(?i)^$|^https://(?:(?:www\\.)?google\\.[a-z.]+/maps(?:[/?#].*)?|maps\\.google\\.[a-z.]+(?:/.*)?|maps\\.app\\.goo\\.gl/.*|goo\\.gl/maps/.*)$") String mapUrl,
        @Min(1) @Max(50) Integer maxGuests,
        @Pattern(regexp = "(?:[01]\\d|2[0-3]):[0-5]\\d") String checkInTime,
        @Pattern(regexp = "(?:[01]\\d|2[0-3]):[0-5]\\d") String checkOutTime,
        @Size(max = 2000) String houseRules,
        @Min(0) @Max(720) Integer cancellationFreeHours,
        @Min(0) @Max(100) Integer refundPercentBeforeDeadline,
        @Min(0) @Max(100) Integer refundPercentWithinDeadline,
        @Min(0) @Max(100) Integer refundPercentAfterCheckIn
) {
}

