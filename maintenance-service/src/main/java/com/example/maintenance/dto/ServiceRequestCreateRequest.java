package com.example.maintenance.dto;

public record ServiceRequestCreateRequest (
        String equipmentId,
        String title,
        String description,
        String status,
        String priority
) {
}
