--liquibase formatted sql

--changeset maintenance-service:001-create-service-request-schema
CREATE TYPE service_request_status AS ENUM (
    'NEW',
    'IN_PROGRESS',
    'DONE',
    'CANCELLED'
);

CREATE TYPE service_request_priority AS ENUM (
    'LOW',
    'MEDIUM',
    'HIGH'
);

CREATE TABLE service_requests (
    id UUID PRIMARY KEY,
    equipment_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(512),
    status service_request_status NOT NULL,
    priority service_request_priority NOT NULL,
    completed_at DATE,
    created_at DATE NOT NULL,
    updated_at DATE NOT NULL,
    etag VARCHAR(27) NOT NULL
);

CREATE INDEX idx_service_requests_equipment_id ON service_requests (equipment_id);
CREATE INDEX idx_service_requests_status ON service_requests (status);
CREATE INDEX idx_service_requests_priority ON service_requests (priority);

--rollback DROP TABLE IF EXISTS service_requests;
--rollback DROP TYPE IF EXISTS service_request_priority;
--rollback DROP TYPE IF EXISTS service_request_status;