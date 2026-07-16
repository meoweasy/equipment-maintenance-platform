package com.example.maintenance.api;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateMaintenanceRequest(
        @NotNull(message = "equipmentId обязателен")
        UUID equipmentId,

        @NotBlank(message = "Описание обязательно")
        String description,

        @NotNull(message = "Дата обслуживания обязательна")
        @FutureOrPresent(message = "Дата не может находиться в прошлом")
        LocalDate scheduledDate
) {
}
