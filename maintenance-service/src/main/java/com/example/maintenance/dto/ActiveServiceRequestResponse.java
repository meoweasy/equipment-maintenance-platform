package com.example.maintenance.dto;

import java.util.UUID;

public record ActiveServiceRequestResponse(
        UUID equipmentId,
        boolean active
) {
}