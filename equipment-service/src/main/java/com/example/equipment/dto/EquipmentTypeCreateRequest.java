package com.example.equipment.dto;

public record EquipmentTypeCreateRequest(
        String name,
        String description,
        String manufacturer,
        Integer maintenanceIntervalDays
) {
}
