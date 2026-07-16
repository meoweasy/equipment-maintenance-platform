package com.example.maintenance.dto;

public record ServiceRequestListFilter(
        String status,
        String priority,
        String equipmentId
) {
}
