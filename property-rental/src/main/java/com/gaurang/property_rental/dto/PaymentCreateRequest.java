package com.gaurang.property_rental.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentCreateRequest(
        @NotNull Long bookingId,
        @NotBlank String paymentMethod,
        @NotBlank String transactionId
) {
}