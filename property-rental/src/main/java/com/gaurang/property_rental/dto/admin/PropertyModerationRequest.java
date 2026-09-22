package com.gaurang.property_rental.dto.admin;

import jakarta.validation.constraints.Pattern;

public record PropertyModerationRequest(
        @Pattern(regexp = "APPROVED|REJECTED", message = "status must be APPROVED or REJECTED") String status
) { }
