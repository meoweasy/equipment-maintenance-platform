package com.example.equipment.entity;

import com.example.equipment.enums.EquipmentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.LocalDate;

@Entity
@Table(name = "equipments")
@Getter
@Setter
@NoArgsConstructor
public class Equipment extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "inventory_number", unique = true, nullable = false)
    private int inventoryNumber;

    @Column(name = "location", nullable = false)
    private String location;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipment_type_id", nullable = false)
    private EquipmentType equipmentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private EquipmentStatus status;

    private LocalDate decommissionedAt;

    public static Equipment create(
            String name,
            int inventoryNumber,
            String location,
            EquipmentType equipmentType
    ) {
        Equipment equipment = new Equipment();
        equipment.name = name;
        equipment.inventoryNumber = inventoryNumber;
        equipment.location = location;
        equipment.equipmentType = equipmentType;
        equipment.status = EquipmentStatus.AVAILABLE;
        return equipment;
    }
}
