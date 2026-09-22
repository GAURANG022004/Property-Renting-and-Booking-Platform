package com.gaurang.property_rental.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PropertyCreateRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String location,
        @NotNull @DecimalMin("0.0") double pricePerNight,
        @NotNull @DecimalMin("0.0") double rating
) {
}

