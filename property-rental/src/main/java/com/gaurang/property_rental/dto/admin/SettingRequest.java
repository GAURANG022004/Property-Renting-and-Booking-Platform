package com.gaurang.property_rental.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SettingRequest(@NotBlank @Size(max = 100) String key, @NotBlank @Size(max = 3000) String value) { }
