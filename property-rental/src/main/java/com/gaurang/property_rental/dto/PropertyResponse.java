package com.gaurang.property_rental.dto;

import java.util.List;

public record PropertyResponse(
        Long id,
        String title,
        String description,
        String location,
        double pricePerNight,
        double rating,
        String approvalStatus,
        String ownerEmail,
        List<String> imageUrls
) {
}
