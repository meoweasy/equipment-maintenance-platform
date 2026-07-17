package com.example.equipment.services;

import com.example.equipment.dto.EquipmentCreateRequest;
import com.example.equipment.dto.EquipmentListFilter;
import com.example.equipment.dto.EquipmentResponse;
import com.example.equipment.entity.Equipment;
import com.example.equipment.entity.EquipmentType;
import com.example.equipment.enums.EquipmentStatus;
import com.example.equipment.mapper.EquipmentMapper;
import com.example.equipment.repository.EquipmentRepository;
import com.example.platform.common.etag.EtagUtils;
import com.example.platform.common.exception.*;
import com.example.platform.common.pagination.PageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static com.example.platform.common.pagination.PaginationConstants.*;

@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentTypeService equipmentTypeService;
    private final EquipmentMapper equipmentMapper;

    @Override
    @Transactional
    public EquipmentResponse create(EquipmentCreateRequest request) {

        if (equipmentRepository.existsByInventoryNumber(request.inventoryNumber())) {
            throw new ResourceAlreadyExistsException("Equipment with inventory number already exists");
        }

        EquipmentType equipmentType = equipmentTypeService.findById(request.equipmentTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipment type", request.equipmentTypeId()));

        Equipment equipment = Equipment.create(
                request.name().trim(),
                request.inventoryNumber(),
                request.location().trim(),
                equipmentType
        );

        Equipment savedEquipment = equipmentRepository.saveAndFlush(equipment);
        return equipmentMapper.toResponse(savedEquipment);
    }

    @Override
    @Transactional(readOnly = true)
    public PageDto<EquipmentResponse> list(
            EquipmentListFilter filter,
            Integer pageSize,
            Integer pageNumber
    ) {
        EquipmentListFilter actualFilter = filter == null
                ? new EquipmentListFilter(null)
                : filter;
        validateList(pageSize, pageNumber);

        pageNumber = pageNumber == null ? DEFAULT_PAGE_NUMBER : pageNumber;
        pageSize = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
        Pageable pageable = PageRequest.of(
                pageNumber,
                pageSize,
                Sort.by(Sort.Direction.ASC, "inventoryNumber")
        );

        Page<Equipment> equipmentPage;
        if (actualFilter.equipmentTypeId() == null) {
            equipmentPage = equipmentRepository.findAll(pageable);
        } else {
            UUID equipmentTypeId = actualFilter.equipmentTypeId();
            EquipmentType equipmentType = equipmentTypeService.findById(equipmentTypeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Equipment type", equipmentTypeId));
            equipmentPage = equipmentRepository.findAllByEquipmentType(equipmentType, pageable);
        }

        return PageDto.of(equipmentPage, equipmentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentResponse getById(UUID id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", id));

        return equipmentMapper.toResponse(equipment);
    }

    @Override
    @Transactional
    public EquipmentResponse update(UUID id, String etag, EquipmentCreateRequest request) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", id));
        EtagUtils.validateIfMatch(etag, equipment.getEtag());

        if (equipment.getInventoryNumber() != request.inventoryNumber()
                && equipmentRepository.existsByInventoryNumber(request.inventoryNumber())) {
            throw new ResourceAlreadyExistsException("Equipment with inventory number already exists");
        }

        EquipmentType equipmentType = equipmentTypeService.findById(request.equipmentTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipment type", request.equipmentTypeId()));

        equipment.setName(request.name().trim());
        equipment.setInventoryNumber(request.inventoryNumber());
        equipment.setLocation(request.location().trim());
        equipment.setEquipmentType(equipmentType);

        return equipmentMapper.toResponse(equipmentRepository.saveAndFlush(equipment));
    }

    @Override
    @Transactional
    public void delete(UUID id, String etag) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", id));
        EtagUtils.validateIfMatch(etag, equipment.getEtag());
        equipmentRepository.delete(equipment);
    }

    @Override
    @Transactional
    public EquipmentResponse changeStatus(UUID id, String etag, EquipmentStatus status) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", id));
        EtagUtils.validateIfMatch(etag, equipment.getEtag());

        if (equipment.getStatus() == EquipmentStatus.UNDER_MAINTENANCE
                && status == EquipmentStatus.DECOMMISSIONED) {
            throw new StatusChangeNotAllowedException("Equipment under maintenance cannot be decommissioned");
        }

        equipment.setStatus(status);
        equipment.setDecommissionedAt(
                status == EquipmentStatus.DECOMMISSIONED ? LocalDate.now() : null
        );

        return equipmentMapper.toResponse(equipmentRepository.saveAndFlush(equipment));
    }

    private void validateList(Integer pageSize, Integer pageNumber) {
        if (pageNumber != null && pageNumber < 0) {
            throw new ValueTooSmallException("Page number", 0);
        }
        if (pageSize != null && pageSize < MIN_PAGE_SIZE) {
            throw new ValueTooSmallException("Page size", MIN_PAGE_SIZE);
        }
        if (pageSize != null && pageSize > MAX_PAGE_SIZE) {
            throw new ValueTooLargeException("Page size", MAX_PAGE_SIZE);
        }
    }
}
