package com.example.equipment.services;

import com.example.equipment.dto.EquipmentCreateRequest;
import com.example.equipment.dto.EquipmentListFilter;
import com.example.equipment.dto.EquipmentResponse;
import com.example.equipment.dto.PageDto;
import com.example.equipment.entity.Equipment;
import com.example.equipment.entity.EquipmentType;
import com.example.equipment.enums.EquipmentStatus;
import com.example.equipment.mapper.EquipmentMapper;
import com.example.equipment.repository.EquipmentRepository;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {

    public final static int PAGE_SIZE_DEFAULT = 20;
    public final static int PAGE_NUMBER_DEFAULT = 0;
    public final static int PAGE_SIZE_MAX = 20;
    public final static int PAGE_SIZE_MIN = 1;

    private final EquipmentRepository equipmentRepository;
    private final EquipmentTypeService equipmentTypeService;
    private final EquipmentMapper equipmentMapper;

    @Override
    @Transactional
    public EquipmentResponse create(EquipmentCreateRequest request) {
        validateCreate(request);

        if (equipmentRepository.existsByInventoryNumber(request.inventoryNumber())) {
            throw new IllegalArgumentException(
                    "Equipment with inventory number already exists"
            );
        }

        UUID equipmentTypeId = validateAndMapId(request.equipmentTypeId(), "EquipmentTypeId");
        EquipmentType equipmentType = equipmentTypeService.getById(equipmentTypeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "EquipmentTypeId is not valid"
                ));

        Equipment equipment = Equipment.create(
                request.name().trim(),
                request.inventoryNumber(),
                request.location().trim(),
                equipmentType
        );

        Equipment savedEquipment = equipmentRepository.save(equipment);
        return equipmentMapper.toResponse(savedEquipment);
    }

    @Override
    @Transactional(readOnly = true)
    public PageDto<EquipmentResponse> list(
            EquipmentListFilter filter,
            Integer pageSize,
            Integer pageNumber
    ) {
        if (filter == null) {
            filter = new EquipmentListFilter(null);
        }
        validateList(pageSize, pageNumber);

        pageNumber = pageNumber == null ? PAGE_NUMBER_DEFAULT : pageNumber;
        pageSize = pageSize == null ? PAGE_SIZE_DEFAULT : pageSize;
        Pageable pageable = PageRequest.of(
                pageNumber,
                pageSize,
                Sort.by(Sort.Direction.ASC, "inventoryNumber")
        );

        Page<Equipment> equipmentPage;
        if (filter.equipmentTypeId() == null) {
            equipmentPage = equipmentRepository.findAll(pageable);
        } else {
            UUID equipmentTypeId = validateAndMapId(filter.equipmentTypeId(), "EquipmentTypeId");
            EquipmentType equipmentType = equipmentTypeService.getById(equipmentTypeId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "EquipmentTypeId is not valid"
                    ));
            equipmentPage = equipmentRepository.findAllByEquipmentType(equipmentType, pageable);
        }

        return PageDto.of(equipmentPage, equipmentMapper::toResponse);
    }
    @Override
    @Transactional(readOnly = true)
    public EquipmentResponse getById(String id) {
        UUID equipmentId = validateAndMapId(id, "Id");

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new RuntimeException (
                        "Equipment with id " + equipmentId + " not found"
                ));

        return equipmentMapper.toResponse(equipment);
    }

    @Override
    @Transactional
    public EquipmentResponse update(String id, EquipmentCreateRequest request) {
        UUID equipmentId = validateAndMapId(id, "Id");
        validateCreate(request);

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Equipment with id " + equipmentId + " not found"
                ));

        if (equipment.getInventoryNumber() != request.inventoryNumber()
                && equipmentRepository.existsByInventoryNumber(request.inventoryNumber())) {
            throw new IllegalArgumentException(
                    "Equipment with inventory number already exists"
            );
        }

        UUID equipmentTypeId = validateAndMapId(request.equipmentTypeId(), "EquipmentTypeId");
        EquipmentType equipmentType = equipmentTypeService.getById(equipmentTypeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "EquipmentTypeId is not valid"
                ));

        equipment.setName(request.name().trim());
        equipment.setInventoryNumber(request.inventoryNumber());
        equipment.setLocation(request.location().trim());
        equipment.setEquipmentType(equipmentType);

        return equipmentMapper.toResponse(equipmentRepository.save(equipment));
    }
    @Override
    @Transactional
    public void delete(String id) {
        UUID equipmentId = validateAndMapId(id, "Id");
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Equipment with id " + equipmentId + " not found"
                ));
        equipmentRepository.delete(equipment);
    }

    @Override
    @Transactional
    public EquipmentResponse changeStatus(String id, String status) {
        UUID equipmentId = validateAndMapId(id, "Id");
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }

        EquipmentStatus equipmentStatus;
        try {
            equipmentStatus = EquipmentStatus.valueOf(status.trim());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Status is not valid");
        }

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Equipment with id " + equipmentId + " not found"
                ));

        if (equipment.getStatus() == EquipmentStatus.UNDER_MAINTENANCE
                && equipmentStatus == EquipmentStatus.DECOMMISSIONED) {
            throw new IllegalArgumentException(
                    "Equipment under maintenance cannot be decommissioned"
            );
        }

        equipment.setStatus(equipmentStatus);
        equipment.setDecommissionedAt(
                equipmentStatus == EquipmentStatus.DECOMMISSIONED ? LocalDate.now() : null
        );

        return equipmentMapper.toResponse(equipmentRepository.save(equipment));
    }

    private void validateList(Integer pageSize, Integer pageNumber) {
        if (pageNumber != null && pageNumber < 0) {
            throw new IllegalArgumentException("Page number must not be negative");
        }
        if (pageSize != null && pageSize < PAGE_SIZE_MIN) {
            throw new IllegalArgumentException("Page size must be at least 1");
        }
        if (pageSize != null && pageSize > PAGE_SIZE_MAX) {
            throw new IllegalArgumentException("Page size is too large");
        }
    }

    private void validateCreate(EquipmentCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create request is required");
        }

        if (request.name() == null || request.name().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (request.name().isBlank()) {
            throw new IllegalArgumentException("Name cannot consist of only spaces");
        }

        if (request.inventoryNumber() == null) {
            throw new IllegalArgumentException("Inventory number is required");
        }
        if (request.inventoryNumber() < 1) {
            throw new IllegalArgumentException("The inventory number must not be less than 1");
        }

        if (request.location() == null || request.location().isEmpty()) {
            throw new IllegalArgumentException("Location is required");
        }
        if (request.location().isBlank()) {
            throw new IllegalArgumentException("Location cannot consist of only spaces");
        }
    }

    private UUID validateAndMapId(String id, String fieldName) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(fieldName + " is not a valid UUID");
        }
    }


}
