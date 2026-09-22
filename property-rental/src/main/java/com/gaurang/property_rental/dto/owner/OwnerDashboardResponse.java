package com.gaurang.property_rental.dto.owner;

public record OwnerDashboardResponse(long totalProperties, long activeProperties, long pendingProperties,
                                     long pendingBookingRequests, long acceptedBookings, long completedStays,
                                     double estimatedEarnings) { }
