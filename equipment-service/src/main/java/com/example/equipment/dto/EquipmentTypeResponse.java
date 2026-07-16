package com.example.equipment.dto;

import java.time.LocalDate;
import java.util.UUID;

public record EquipmentTypeResponse(
        UUID id,
        String name,
        String description,
        String manufacturer,
        int maintenanceIntervalDays,
        LocalDate createdAt,
        LocalDate updatedAt,
        String etag
) {
}
