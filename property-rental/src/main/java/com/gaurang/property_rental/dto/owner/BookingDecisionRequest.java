package com.gaurang.property_rental.dto.owner;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;

public record BookingDecisionRequest(
        @NotBlank @Pattern(regexp = "ACCEPTED|REJECTED", message = "status must be ACCEPTED or REJECTED") String status
) { }
