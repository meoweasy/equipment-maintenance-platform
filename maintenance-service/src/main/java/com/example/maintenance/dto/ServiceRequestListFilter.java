package com.example.maintenance.dto;

import com.example.maintenance.enums.Priority;
import com.example.maintenance.enums.ServiceRequestStatus;

import java.util.UUID;

public record ServiceRequestListFilter(
        ServiceRequestStatus status,
        Priority priority,
        UUID equipmentId
) {
}
