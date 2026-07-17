package com.example.equipment.services;

import com.example.equipment.dto.EquipmentTypeCreateRequest;
import com.example.equipment.dto.EquipmentTypeListFilter;
import com.example.equipment.dto.EquipmentTypeResponse;
import com.example.platform.common.pagination.PageDto;
import com.example.equipment.entity.EquipmentType;
import com.example.equipment.mapper.EquipmentMapper;
import com.example.platform.common.exception.InvalidIdException;
import com.example.platform.common.exception.RequiredFieldException;
import com.example.platform.common.exception.ResourceAlreadyExistsException;
import com.example.platform.common.exception.ResourceNotFoundException;
import com.example.platform.common.exception.ValueTooLargeException;
import com.example.platform.common.exception.ValueTooSmallException;
import com.example.equipment.repository.EquipmentTypeRepository;
import com.example.platform.common.etag.EtagUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static com.example.platform.common.pagination.PaginationConstants.DEFAULT_PAGE_NUMBER;
import static com.example.platform.common.pagination.PaginationConstants.DEFAULT_PAGE_SIZE;
import static com.example.platform.common.pagination.PaginationConstants.MAX_PAGE_SIZE;
import static com.example.platform.common.pagination.PaginationConstants.MIN_PAGE_SIZE;

@Service
@RequiredArgsConstructor
public class EquipmentTypeServiceImpl implements EquipmentTypeService {

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

        pageNumber = pageNumber == null ? DEFAULT_PAGE_NUMBER : pageNumber;
        pageSize = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
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