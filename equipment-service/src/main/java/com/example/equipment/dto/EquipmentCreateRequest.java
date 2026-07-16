package com.example.equipment.dto;

public record EquipmentCreateRequest(
        String name,
        Integer inventoryNumber,
        String location,
        String equipmentTypeId
) {
}
