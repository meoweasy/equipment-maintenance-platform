package com.example.equipment.services;

import com.example.equipment.dto.EquipmentCreateRequest;
import com.example.equipment.dto.EquipmentListFilter;
import com.example.equipment.dto.EquipmentResponse;
import com.example.equipment.dto.PageDto;
import com.example.equipment.enums.EquipmentStatus;

public interface EquipmentService {

    EquipmentResponse create(EquipmentCreateRequest request);

    PageDto<EquipmentResponse> list(
            EquipmentListFilter filter,
            Integer pageSize,
            Integer pageNumber
    );

    EquipmentResponse getById(String id);

    EquipmentResponse update(String id, EquipmentCreateRequest request);

    void delete(String id);

    EquipmentResponse changeStatus(String id, String status);
}
