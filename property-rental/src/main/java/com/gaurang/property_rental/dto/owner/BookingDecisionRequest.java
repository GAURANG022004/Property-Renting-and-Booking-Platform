package com.gaurang.property_rental.dto.owner;

import jakarta.validation.constraints.Pattern;

public record BookingDecisionRequest(
        @Pattern(regexp = "ACCEPTED|REJECTED", message = "status must be ACCEPTED or REJECTED") String status
) { }
