package com.example.equipment.services;

import com.example.equipment.dto.EquipmentTypeCreateRequest;
import com.example.equipment.dto.EquipmentTypeListFilter;
import com.example.equipment.dto.EquipmentTypeResponse;
import com.example.platform.common.pagination.PageDto;
import com.example.equipment.entity.EquipmentType;

import java.util.Optional;
import java.util.UUID;

public interface EquipmentTypeService {

    Optional<EquipmentType> getById(UUID id);

    EquipmentTypeResponse create(EquipmentTypeCreateRequest request);

    PageDto<EquipmentTypeResponse> list(
            EquipmentTypeListFilter filter,
            Integer pageSize,
            Integer pageNumber
    );

    EquipmentTypeResponse getById(String id);

    EquipmentTypeResponse update(String id, String etag, EquipmentTypeCreateRequest request);

    void delete(String id, String etag);

}
