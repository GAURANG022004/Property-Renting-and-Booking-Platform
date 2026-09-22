package com.gaurang.property_rental.dto.admin;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ComplaintResolutionRequest(
        @Pattern(regexp = "IN_PROGRESS|RESOLVED|DISMISSED", message = "status must be IN_PROGRESS, RESOLVED, or DISMISSED") String status,
        @Size(max = 3000) String resolutionNote
) { }
