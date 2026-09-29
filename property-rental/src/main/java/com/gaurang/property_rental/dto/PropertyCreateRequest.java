package com.gaurang.property_rental.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PropertyCreateRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String location,
        @NotNull @DecimalMin("0.0") double pricePerNight,
        @Size(max = 2048) @Pattern(regexp = "(?i)^$|^https://(?:(?:www\\.)?google\\.[a-z.]+/maps(?:[/?#].*)?|maps\\.google\\.[a-z.]+(?:/.*)?|maps\\.app\\.goo\\.gl/.*|goo\\.gl/maps/.*)$") String mapUrl
) {
}

