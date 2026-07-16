package com.example.equipment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "equipment_types")
@Getter
@Setter
@NoArgsConstructor
public class EquipmentType extends BaseEntity {

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "manufacturer", nullable = false)
    private String manufacturer;

    @Column(name = "maintenance_interval_days", nullable = false)
    private int maintenanceIntervalDays;
}
