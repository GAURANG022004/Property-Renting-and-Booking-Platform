package com.gaurang.property_rental.dto.admin;

import java.util.Set;

public record AdminUserResponse(Long id, String firstName, String lastName, String email, String phoneNumber,
                                Set<String> roles, boolean active) { }
