package com.example.equipment.services;

import com.example.equipment.dto.EquipmentCreateRequest;
import com.example.equipment.dto.EquipmentListFilter;
import com.example.equipment.dto.EquipmentResponse;
import com.example.platform.common.pagination.PageDto;
import com.example.equipment.enums.EquipmentStatus;

public interface EquipmentService {

    EquipmentResponse create(EquipmentCreateRequest request);

    PageDto<EquipmentResponse> list(
            EquipmentListFilter filter,
            Integer pageSize,
            Integer pageNumber
    );

    EquipmentResponse getById(String id);

    EquipmentResponse update(String id, String etag, EquipmentCreateRequest request);

    void delete(String id, String etag);

    EquipmentResponse changeStatus(String id, String etag, String status);
}
