package com.example.equipment.dto;

import com.example.equipment.enums.EquipmentStatus;
import com.example.equipment.utils.ksuuid.KsuidVersion;

import java.time.LocalDate;
import java.util.UUID;

public record EquipmentResponse(
        UUID id,
        EquipmentTypeResponse equipmentType,
        String name,
        int inventoryNumber,
        String location,
        EquipmentStatus status,
        LocalDate createdAt,
        LocalDate decommissionedAt,
        KsuidVersion etag
) {
}
