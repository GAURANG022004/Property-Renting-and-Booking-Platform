package com.gaurang.property_rental.dto.owner;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public record OwnerPropertyUpdateRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 2000) String description,
        @NotBlank @Size(max = 255) String location,
        @DecimalMin("0.0") double pricePerNight,
        @Size(max = 2048) @Pattern(regexp = "(?i)^$|^https://(?:(?:www\\.)?google\\.[a-z.]+/maps(?:[/?#].*)?|maps\\.google\\.[a-z.]+(?:/.*)?|maps\\.app\\.goo\\.gl/.*|goo\\.gl/maps/.*)$") String mapUrl,
        @Min(1) @Max(50) Integer maxGuests,
        @Pattern(regexp = "(?:[01]\\d|2[0-3]):[0-5]\\d") String checkInTime,
        @Pattern(regexp = "(?:[01]\\d|2[0-3]):[0-5]\\d") String checkOutTime,
        @Size(max = 2000) String houseRules,
        @Min(0) @Max(720) Integer cancellationFreeHours,
        @Min(0) @Max(100) Integer refundPercentBeforeDeadline,
        @Min(0) @Max(100) Integer refundPercentWithinDeadline,
        @Min(0) @Max(100) Integer refundPercentAfterCheckIn
) { }
