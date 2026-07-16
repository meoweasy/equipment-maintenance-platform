package com.example.equipment.services;

import com.example.equipment.dto.EquipmentTypeCreateRequest;
import com.example.equipment.dto.EquipmentTypeListFilter;
import com.example.equipment.dto.EquipmentTypeResponse;
import com.example.equipment.dto.PageDto;
import com.example.equipment.entity.EquipmentType;
import com.example.equipment.mapper.EquipmentMapper;
import com.example.equipment.exception.BlankFieldException;
import com.example.equipment.exception.InvalidIdException;
import com.example.equipment.exception.RequiredFieldException;
import com.example.equipment.exception.ResourceAlreadyExistsException;
import com.example.equipment.exception.ResourceNotFoundException;
import com.example.equipment.exception.ValueTooLargeException;
import com.example.equipment.exception.ValueTooSmallException;
import com.example.equipment.repository.EquipmentTypeRepository;
import com.example.equipment.utils.EtagUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EquipmentTypeServiceImpl implements EquipmentTypeService {

    public static final int PAGE_SIZE_DEFAULT = 20;
    public static final int PAGE_NUMBER_DEFAULT = 0;
    public static final int PAGE_SIZE_MAX = 20;
    public static final int PAGE_SIZE_MIN = 1;

    private final EquipmentTypeRepository equipmentTypeRepository;
    private final EquipmentMapper equipmentMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<EquipmentType> getById(UUID id) {
        return equipmentTypeRepository.findById(id);
    }

    @Override
    @Transactional
    public EquipmentTypeResponse create(EquipmentTypeCreateRequest request) {
        validateCreate(request);

        String name = request.name().trim();
        if (equipmentTypeRepository.existsByName(name)) {
            throw new ResourceAlreadyExistsException("Equipment type with name already exists");
        }

        EquipmentType equipmentType = new EquipmentType();
        equipmentType.setName(request.name().trim());
        equipmentType.setDescription(
                request.description() == null ? null : request.description().trim()
        );
        equipmentType.setManufacturer(request.manufacturer().trim());
        equipmentType.setMaintenanceIntervalDays(request.maintenanceIntervalDays());

        return equipmentMapper.toTypeResponse(equipmentTypeRepository.saveAndFlush(equipmentType));
    }

    @Override
    @Transactional(readOnly = true)
    public PageDto<EquipmentTypeResponse> list(
            EquipmentTypeListFilter filter,
            Integer pageSize,
            Integer pageNumber
    ) {
        validateList(pageSize, pageNumber);

        pageNumber = pageNumber == null ? PAGE_NUMBER_DEFAULT : pageNumber;
        pageSize = pageSize == null ? PAGE_SIZE_DEFAULT : pageSize;
        Pageable pageable = PageRequest.of(
                pageNumber,
                pageSize,
                Sort.by(Sort.Direction.ASC, "name")
        );

        return PageDto.of(
                equipmentTypeRepository.findAll(pageable),
                equipmentMapper::toTypeResponse
        );
    }
    @Override
    @Transactional(readOnly = true)
    public EquipmentTypeResponse getById(String id) {
        UUID equipmentTypeId = validateAndMapId(id, "Id");
        EquipmentType equipmentType = equipmentTypeRepository.findById(equipmentTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment type", equipmentTypeId));
        return equipmentMapper.toTypeResponse(equipmentType);
    }

    @Override
    @Transactional
    public EquipmentTypeResponse update(String id, String etag, EquipmentTypeCreateRequest request) {
        if (id == null || id.isEmpty()) {
            throw new RequiredFieldException("Id");
        }
        UUID equipmentTypeId;
        try {
            equipmentTypeId = UUID.fromString(id);
        } catch (IllegalArgumentException exception) {
            throw new InvalidIdException("Id");
        }
        validateCreate(request);

        EquipmentType equipmentType = equipmentTypeRepository.findById(equipmentTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment type", equipmentTypeId));
        EtagUtils.validateIfMatch(etag, equipmentType.getEtag());

        String name = request.name().trim();
        if (!equipmentType.getName().equals(name)
                && equipmentTypeRepository.existsByName(name)) {
            throw new ResourceAlreadyExistsException("Equipment type with name already exists");
        }

        equipmentType.setName(name);
        equipmentType.setDescription(
                request.description() == null ? null : request.description().trim()
        );
        equipmentType.setManufacturer(request.manufacturer().trim());
        equipmentType.setMaintenanceIntervalDays(request.maintenanceIntervalDays());

        return equipmentMapper.toTypeResponse(equipmentTypeRepository.saveAndFlush(equipmentType));
    }
    @Override
    @Transactional
    public void delete(String id, String etag) {
        UUID equipmentTypeId = validateAndMapId(id, "Id");

        EquipmentType equipmentType = equipmentTypeRepository.findById(equipmentTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment type", equipmentTypeId));
        EtagUtils.validateIfMatch(etag, equipmentType.getEtag());
        equipmentTypeRepository.delete(equipmentType);
    }

    private void validateCreate(EquipmentTypeCreateRequest request) {
        if (request == null) {
            throw new RequiredFieldException("Create request");
        }
        if (request.name() == null || request.name().isEmpty()) {
            throw new RequiredFieldException("Name");
        }
        if (request.name().isBlank()) {
            throw new BlankFieldException("Name");
        }

        if (request.manufacturer() == null || request.manufacturer().isEmpty()) {
            throw new RequiredFieldException("Manufacturer");
        }
        if (request.manufacturer().isBlank()) {
            throw new BlankFieldException("Manufacturer");
        }

        if (request.maintenanceIntervalDays() == null) {
            throw new RequiredFieldException("Maintenance interval days");
        }
        if (request.maintenanceIntervalDays() < 1) {
            throw new ValueTooSmallException("Maintenance interval days", 1);
        }

        if (request.description() != null && request.description().isBlank()) {
            throw new BlankFieldException("Description");
        }
    }

    private void validateList(Integer pageSize, Integer pageNumber) {
        if (pageNumber != null && pageNumber < 0) {
            throw new ValueTooSmallException("Page number", 0);
        }
        if (pageSize != null && pageSize < PAGE_SIZE_MIN) {
            throw new ValueTooSmallException("Page size", PAGE_SIZE_MIN);
        }
        if (pageSize != null && pageSize > PAGE_SIZE_MAX) {
            throw new ValueTooLargeException("Page size", PAGE_SIZE_MAX);
        }
    }

    private UUID validateAndMapId(String id, String fieldName) {
        if (id == null || id.isEmpty()) {
            throw new RequiredFieldException(fieldName);
        }
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new InvalidIdException(fieldName);
        }
    }

}