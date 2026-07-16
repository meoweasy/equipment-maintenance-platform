package com.example.equipment.mapper;

import com.example.equipment.dto.EquipmentResponse;
import com.example.equipment.dto.EquipmentTypeResponse;
import com.example.equipment.entity.Equipment;
import com.example.equipment.entity.EquipmentType;
import org.springframework.stereotype.Component;

@Component
public class EquipmentMapper {

    public EquipmentResponse toResponse(Equipment equipment) {
        return new EquipmentResponse(
                equipment.getId(),
                toTypeResponse(equipment.getEquipmentType()),
                equipment.getName(),
                equipment.getInventoryNumber(),
                equipment.getLocation(),
                equipment.getStatus(),
                equipment.getCreatedAt(),
                equipment.getDecommissionedAt(),
                equipment.getEtag()
        );
    }

    public EquipmentTypeResponse toTypeResponse(EquipmentType equipmentType) {
        return new EquipmentTypeResponse(
                equipmentType.getId(),
                equipmentType.getName(),
                equipmentType.getDescription(),
                equipmentType.getManufacturer(),
                equipmentType.getMaintenanceIntervalDays(),
                equipmentType.getCreatedAt(),
                equipmentType.getUpdatedAt(),
                equipmentType.getEtag()
        );
    }
}