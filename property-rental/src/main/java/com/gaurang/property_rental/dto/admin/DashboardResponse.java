package com.gaurang.property_rental.dto.admin;

public record DashboardResponse(long totalUsers, long activeUsers, long totalOwners, long totalProperties,
                                long pendingProperties, long totalBookings, long cancelledBookings, long openComplaints) { }
