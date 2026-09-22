package com.gaurang.property_rental.dto.admin;

public record ComplaintResponse(Long id, String reportedByEmail, String subject, String description, String status,
                                String resolutionNote) { }
