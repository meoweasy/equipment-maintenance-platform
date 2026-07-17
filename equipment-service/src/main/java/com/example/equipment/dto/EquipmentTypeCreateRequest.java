package com.example.equipment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record EquipmentTypeCreateRequest(
        @NotBlank(message = "Name is required")
        String name,

        @Pattern(regexp = "(?s).*\\S.*", message = "Description must not be blank")
        String description,

        @NotBlank(message = "Manufacturer is required")
        String manufacturer,

        @NotNull(message = "Maintenance interval days is required")
        @Min(value = 1, message = "Maintenance interval days must not be less than 1")
        Integer maintenanceIntervalDays
) {
}