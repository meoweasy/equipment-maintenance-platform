package com.example.maintenance.dto;

import com.example.maintenance.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ServiceRequestCreateRequest(
        @NotNull(message = "Equipment id is required")
        UUID equipmentId,

        @NotBlank(message = "Title is required")
        String title,

        String description,

        @NotNull(message = "Priority is required")
        Priority priority
) {
}