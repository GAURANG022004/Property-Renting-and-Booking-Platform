package com.gaurang.property_rental.dto;

import jakarta.validation.constraints.NotNull;
public record RazorpayOrderRequest(
        @NotNull Long bookingId
) { }