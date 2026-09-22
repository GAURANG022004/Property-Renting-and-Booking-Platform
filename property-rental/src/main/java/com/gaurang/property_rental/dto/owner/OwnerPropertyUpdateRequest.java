package com.gaurang.property_rental.dto.owner;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OwnerPropertyUpdateRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 2000) String description,
        @NotBlank @Size(max = 255) String location,
        @DecimalMin("0.0") double pricePerNight
) { }
