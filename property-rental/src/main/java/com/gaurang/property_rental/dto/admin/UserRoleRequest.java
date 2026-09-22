package com.gaurang.property_rental.dto.admin;

import jakarta.validation.constraints.Pattern;

public record UserRoleRequest(
        @Pattern(regexp = "OWNER|TENANT", message = "role must be OWNER or TENANT") String role
) { }
