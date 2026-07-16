package com.example.equipment.repository;

import com.example.equipment.entity.EquipmentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EquipmentTypeRepository extends JpaRepository<EquipmentType, UUID> {
    boolean existsByName(String name);
}
