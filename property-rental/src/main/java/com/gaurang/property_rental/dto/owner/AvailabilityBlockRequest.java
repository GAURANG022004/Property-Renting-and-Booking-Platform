package com.gaurang.property_rental.dto.owner;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record AvailabilityBlockRequest(
        @NotNull Long propertyId,
        @NotNull @FutureOrPresent @JsonFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
        @NotNull @FutureOrPresent @JsonFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
        @Size(max = 300) String reason
) { }
