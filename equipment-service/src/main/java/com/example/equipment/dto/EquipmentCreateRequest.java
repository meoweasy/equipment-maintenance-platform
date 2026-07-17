package com.example.equipment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EquipmentCreateRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotNull(message = "Inventory number is required")
        @Min(value = 1, message = "Inventory number must not be less than 1")
        Integer inventoryNumber,

        @NotBlank(message = "Location is required")
        String location,

        @NotNull(message = "Equipment type id is required")
        UUID equipmentTypeId
) {
}