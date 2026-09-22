package com.gaurang.property_rental.dto.owner;

import java.time.LocalDate;
public record AvailabilityBlockResponse(Long id, Long propertyId, LocalDate startDate, LocalDate endDate, String reason) { }
