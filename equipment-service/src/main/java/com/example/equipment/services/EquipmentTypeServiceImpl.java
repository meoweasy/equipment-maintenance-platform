package com.example.equipment.services;

import com.example.equipment.dto.EquipmentTypeCreateRequest;
import com.example.equipment.dto.EquipmentTypeResponse;
import com.example.equipment.entity.EquipmentType;
import com.example.equipment.mapper.EquipmentMapper;
import com.example.equipment.repository.EquipmentTypeRepository;
import com.example.platform.common.etag.EtagUtils;
import com.example.platform.common.exception.ResourceAlreadyExistsException;
import com.example.platform.common.exception.ResourceNotFoundException;
import com.example.platform.common.pagination.PageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static com.example.platform.common.pagination.PaginationConstants.*;

@Service
@RequiredArgsConstructor
public class EquipmentTypeServiceImpl implements EquipmentTypeService {

    private final EquipmentTypeRepository equipmentTypeRepository;
    private final EquipmentMapper equipmentMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<EquipmentType> findById(UUID id) {
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
            Integer pageSize,
            Integer pageNumber
    ) {

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
    public EquipmentTypeResponse getById(UUID id) {
        EquipmentType equipmentType = equipmentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment type", id));
        return equipmentMapper.toTypeResponse(equipmentType);
    }

    @Override
    @Transactional
    public EquipmentTypeResponse update(UUID id, String etag, EquipmentTypeCreateRequest request) {
        EquipmentType equipmentType = equipmentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment type", id));
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
    public void delete(UUID id, String etag) {
        EquipmentType equipmentType = equipmentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment type", id));
        EtagUtils.validateIfMatch(etag, equipmentType.getEtag());
        equipmentTypeRepository.delete(equipmentType);
    }

}