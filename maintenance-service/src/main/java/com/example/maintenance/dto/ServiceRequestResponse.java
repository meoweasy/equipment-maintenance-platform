package com.example.maintenance.dto;

import com.example.maintenance.enums.Priority;
import com.example.maintenance.enums.ServiceRequestStatus;

import java.time.LocalDate;
import java.util.UUID;

public record ServiceRequestResponse(
        UUID id,
        EquipmentResponse equipment,
        String title,
        String description,
        ServiceRequestStatus status,
        Priority priority,
        LocalDate completedAt
) {
    public record EquipmentResponse(
            UUID id,
            EquipmentTypeResponse equipmentType,
            String name,
            int inventoryNumber,
            String location,
            String status,
            LocalDate createdAt,
            LocalDate decommissionedAt,
            String etag
    ) {
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
    }
}
