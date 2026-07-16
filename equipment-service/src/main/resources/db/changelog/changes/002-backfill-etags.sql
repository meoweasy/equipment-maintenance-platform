--liquibase formatted sql

--changeset equipment-service:002-backfill-etags
UPDATE equipment_types
SET etag = SUBSTRING(MD5(RANDOM()::text || CLOCK_TIMESTAMP()::text || id::text), 1, 27)
WHERE etag IS NULL;

UPDATE equipments
SET etag = SUBSTRING(MD5(RANDOM()::text || CLOCK_TIMESTAMP()::text || id::text), 1, 27)
WHERE etag IS NULL;

ALTER TABLE equipment_types ALTER COLUMN etag SET NOT NULL;
ALTER TABLE equipments ALTER COLUMN etag SET NOT NULL;

--rollback ALTER TABLE equipments ALTER COLUMN etag DROP NOT NULL;
--rollback ALTER TABLE equipment_types ALTER COLUMN etag DROP NOT NULL;
