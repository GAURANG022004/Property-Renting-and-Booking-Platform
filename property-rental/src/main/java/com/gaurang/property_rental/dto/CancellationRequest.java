package com.gaurang.property_rental.dto;

import jakarta.validation.constraints.AssertTrue;

public record CancellationRequest(@AssertTrue boolean acknowledged) { }