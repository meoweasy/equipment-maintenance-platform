package com.example.equipment.services;

import com.example.equipment.dto.EquipmentTypeCreateRequest;
import com.example.equipment.dto.EquipmentTypeResponse;
import com.example.equipment.entity.EquipmentType;
import com.example.platform.common.pagination.PageDto;

import java.util.Optional;
import java.util.UUID;

public interface EquipmentTypeService {

    Optional<EquipmentType> findById(UUID id);

    EquipmentTypeResponse create(EquipmentTypeCreateRequest request);

    PageDto<EquipmentTypeResponse> list(
            Integer pageSize,
            Integer pageNumber
    );

    EquipmentTypeResponse getById(UUID id);

    EquipmentTypeResponse update(UUID id, String etag, EquipmentTypeCreateRequest request);

    void delete(UUID id, String etag);

}
