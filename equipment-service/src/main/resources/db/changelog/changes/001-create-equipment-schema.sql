--liquibase formatted sql

--changeset equipment-service:001-create-equipment-schema
CREATE TYPE equipment_status AS ENUM (
    'AVAILABLE',
    'UNDER_MAINTENANCE',
    'DECOMMISSIONED'
);

CREATE TABLE equipment_types (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(512),
    manufacturer VARCHAR(255) NOT NULL,
    maintenance_interval_days INTEGER NOT NULL,
    created_at DATE NOT NULL,
    updated_at DATE NOT NULL,
    etag VARCHAR(27),
    CONSTRAINT uk_equipment_types_name UNIQUE (name)
);

CREATE TABLE equipments (
    id UUID PRIMARY KEY,
    equipment_type_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    inventory_number INTEGER NOT NULL,
    location VARCHAR(255) NOT NULL,
    status equipment_status NOT NULL,
    decommissioned_at DATE,
    created_at DATE NOT NULL,
    updated_at DATE NOT NULL,
    etag VARCHAR(27),
    CONSTRAINT uk_equipments_inventory_number UNIQUE (inventory_number),
    CONSTRAINT fk_equipments_equipment_type
        FOREIGN KEY (equipment_type_id)
        REFERENCES equipment_types (id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_equipments_equipment_type_id
    ON equipments (equipment_type_id);

CREATE INDEX idx_equipments_status
    ON equipments (status);

--rollback DROP TABLE IF EXISTS equipments;
--rollback DROP TABLE IF EXISTS equipment_types;
--rollback DROP TYPE IF EXISTS equipment_status;