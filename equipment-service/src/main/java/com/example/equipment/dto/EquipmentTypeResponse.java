package com.example.equipment.dto;

import com.example.equipment.utils.ksuuid.KsuidVersion;

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
        KsuidVersion etag
) {
}
