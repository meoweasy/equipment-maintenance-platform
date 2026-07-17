package com.example.equipment.services;

import com.example.equipment.dto.EquipmentCreateRequest;
import com.example.equipment.dto.EquipmentListFilter;
import com.example.equipment.dto.EquipmentResponse;
import com.example.equipment.enums.EquipmentStatus;
import com.example.platform.common.pagination.PageDto;

import java.util.UUID;

public interface EquipmentService {

    EquipmentResponse create(EquipmentCreateRequest request);

    PageDto<EquipmentResponse> list(
            EquipmentListFilter filter,
            Integer pageSize,
            Integer pageNumber
    );

    EquipmentResponse getById(UUID id);

    EquipmentResponse update(UUID id, String etag, EquipmentCreateRequest request);

    void delete(UUID id, String etag);

    EquipmentResponse changeStatus(UUID id, String etag, EquipmentStatus status);
}
