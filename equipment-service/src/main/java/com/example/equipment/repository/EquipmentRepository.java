package com.example.equipment.repository;

import com.example.equipment.entity.Equipment;
import com.example.equipment.entity.EquipmentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, UUID> {
    boolean existsByInventoryNumber(int inventoryNumber);

    boolean existsByEquipmentType(EquipmentType equipmentType);

    Page<Equipment> findAllByEquipmentType(EquipmentType equipmentType, Pageable pageable);
}
