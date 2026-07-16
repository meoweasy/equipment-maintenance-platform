package com.example.equipment.dto;

import com.example.equipment.enums.EquipmentStatus;
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
        String etag
) {
}
