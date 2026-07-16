package com.example.maintenance.domain;

import java.time.LocalDate;
import java.util.UUID;

public record MaintenanceRequest(
        UUID id,
        UUID equipmentId,
        String description,
        LocalDate scheduledDate,
        MaintenanceStatus status
) {
}
