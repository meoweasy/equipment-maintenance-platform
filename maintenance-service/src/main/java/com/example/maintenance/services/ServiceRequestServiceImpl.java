package com.example.maintenance.services;

import com.example.maintenance.client.EquipmentClient;
import com.example.maintenance.dto.ServiceRequestCreateRequest;
import com.example.maintenance.dto.ServiceRequestListFilter;
import com.example.maintenance.dto.ServiceRequestResponse;
import com.example.maintenance.dto.ServiceRequestResponse.EquipmentResponse;
import com.example.maintenance.entity.ServiceRequest;
import com.example.maintenance.enums.Priority;
import com.example.maintenance.enums.ServiceRequestStatus;
import com.example.maintenance.mapper.ServiceRequestMapper;
import com.example.maintenance.repository.ServiceRequestRepository;
import com.example.platform.common.exception.InvalidFieldValueException;
import com.example.platform.common.exception.InvalidIdException;
import com.example.platform.common.exception.RequiredFieldException;
import com.example.platform.common.exception.ResourceNotFoundException;
import com.example.platform.common.exception.PreconditionFailedException;
import com.example.platform.common.exception.ValueTooLargeException;
import com.example.platform.common.exception.ValueTooSmallException;
import com.example.platform.common.pagination.PageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static com.example.platform.common.pagination.PaginationConstants.DEFAULT_PAGE_NUMBER;
import static com.example.platform.common.pagination.PaginationConstants.DEFAULT_PAGE_SIZE;
import static com.example.platform.common.pagination.PaginationConstants.MAX_PAGE_SIZE;
import static com.example.platform.common.pagination.PaginationConstants.MIN_PAGE_SIZE;

@Service
@RequiredArgsConstructor
public class ServiceRequestServiceImpl implements ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final EquipmentClient equipmentClient;
    private final ServiceRequestMapper serviceRequestMapper;

    @Override
    @Transactional
    public ServiceRequestResponse create(ServiceRequestCreateRequest request) {
        UUID equipmentId = validateAndMapId(request.equipmentId(), "EquipmentId");
        Priority priority = validateAndMapPriority(request.priority());
        EquipmentResponse equipment = equipmentClient.getFresh(equipmentId);
        if ("DECOMMISSIONED".equals(equipment.status())) {
            throw new PreconditionFailedException(
                    "Service request cannot be created for decommissioned equipment"
            );
        }

        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setEquipmentId(equipmentId);
        serviceRequest.setTitle(request.title().trim());
        String description = request.description() == null ? null : request.description().trim();
        serviceRequest.setDescription(description == null || description.isEmpty() ? null : description);
        serviceRequest.setPriority(priority);
        serviceRequest.setStatus(ServiceRequestStatus.NEW);
        serviceRequest.setCompletedAt(null);

        return serviceRequestMapper.toResponse(serviceRequestRepository.saveAndFlush(serviceRequest), equipment);
    }

    @Override
    @Transactional(readOnly = true)
    public PageDto<ServiceRequestResponse> list(
            ServiceRequestListFilter filter,
            Integer pageSize,
            Integer pageNumber
    ) {
        validatePagination(pageSize, pageNumber);
        ServiceRequestListFilter effectiveFilter = filter == null
                ? new ServiceRequestListFilter(null, null, null)
                : filter;
        Pageable pageable = PageRequest.of(
                pageNumber == null ? DEFAULT_PAGE_NUMBER : pageNumber,
                pageSize == null ? DEFAULT_PAGE_SIZE : pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Specification<ServiceRequest> specification = (root, query, builder) -> builder.conjunction();
        if (effectiveFilter.status() != null && !effectiveFilter.status().isBlank()) {
            ServiceRequestStatus status = validateAndMapStatus(effectiveFilter.status());
            specification = specification.and((root, query, builder) -> builder.equal(root.get("status"), status));
        }
        if (effectiveFilter.priority() != null && !effectiveFilter.priority().isBlank()) {
            Priority priority = validateAndMapPriority(effectiveFilter.priority());
            specification = specification.and((root, query, builder) -> builder.equal(root.get("priority"), priority));
        }
        if (effectiveFilter.equipmentId() != null && !effectiveFilter.equipmentId().isBlank()) {
            UUID equipmentId = validateAndMapId(effectiveFilter.equipmentId(), "EquipmentId");
            specification = specification.and((root, query, builder) -> builder.equal(root.get("equipmentId"), equipmentId));
        }

        Page<ServiceRequest> serviceRequests = serviceRequestRepository.findAll(specification, pageable);
        return PageDto.of(serviceRequests, serviceRequest -> serviceRequestMapper.toResponse(
                serviceRequest,
                equipmentClient.getCached(serviceRequest.getEquipmentId())
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceRequestResponse getById(String id) {
        UUID serviceRequestId = validateAndMapId(id, "Id");
        ServiceRequest serviceRequest = serviceRequestRepository.findById(serviceRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Service request", serviceRequestId));
        return serviceRequestMapper.toResponse(
                serviceRequest,
                equipmentClient.getCached(serviceRequest.getEquipmentId())
        );
    }

    @Override
    @Transactional
    public ServiceRequestResponse update(String id, ServiceRequestCreateRequest request) {
        UUID serviceRequestId = validateAndMapId(id, "Id");
        ServiceRequest serviceRequest = serviceRequestRepository.findById(serviceRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Service request", serviceRequestId));
        if (serviceRequest.getStatus() == ServiceRequestStatus.DONE) {
            throw new PreconditionFailedException("Completed service request cannot be modified");
        }

        UUID equipmentId = validateAndMapId(request.equipmentId(), "EquipmentId");
        Priority priority = validateAndMapPriority(request.priority());
        EquipmentResponse equipment = equipmentClient.getFresh(equipmentId);
        if ("DECOMMISSIONED".equals(equipment.status())) {
            throw new PreconditionFailedException(
                    "Service request cannot be created for decommissioned equipment"
            );
        }

        serviceRequest.setEquipmentId(equipmentId);
        serviceRequest.setTitle(request.title().trim());
        String description = request.description() == null ? null : request.description().trim();
        serviceRequest.setDescription(description == null || description.isEmpty() ? null : description);
        serviceRequest.setPriority(priority);

        return serviceRequestMapper.toResponse(serviceRequestRepository.saveAndFlush(serviceRequest), equipment);
    }

    @Override
    @Transactional
    public void delete(String id) {
        UUID serviceRequestId = validateAndMapId(id, "Id");
        ServiceRequest serviceRequest = serviceRequestRepository.findById(serviceRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Service request", serviceRequestId));
        if (serviceRequest.getStatus() == ServiceRequestStatus.DONE) {
            throw new PreconditionFailedException("Completed service request cannot be modified");
        }
        if (serviceRequest.getStatus() == ServiceRequestStatus.IN_PROGRESS) {
            throw new PreconditionFailedException("Service request in progress cannot be deleted");
        }
        serviceRequestRepository.delete(serviceRequest);
    }

    @Override
    @Transactional
    public ServiceRequestResponse changeStatus(String id, String status) {
        ServiceRequestStatus newStatus = validateAndMapStatus(status);
        UUID serviceRequestId = validateAndMapId(id, "Id");
        ServiceRequest serviceRequest = serviceRequestRepository.findById(serviceRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Service request", serviceRequestId));
        ServiceRequestStatus currentStatus = serviceRequest.getStatus();

        if (serviceRequest.getStatus() == ServiceRequestStatus.DONE) {
            throw new PreconditionFailedException("Completed service request cannot be modified");
        }
        if (currentStatus == ServiceRequestStatus.CANCELLED
                && newStatus == ServiceRequestStatus.IN_PROGRESS) {
            throw new PreconditionFailedException(
                    "Cancelled service request cannot be moved to in progress"
            );
        }

        serviceRequest.setStatus(newStatus);
        if (newStatus == ServiceRequestStatus.DONE) {
            serviceRequest.setCompletedAt(LocalDate.now());
        }

        ServiceRequest savedServiceRequest = serviceRequestRepository.saveAndFlush(serviceRequest);
        return serviceRequestMapper.toResponse(
                savedServiceRequest,
                equipmentClient.getCached(savedServiceRequest.getEquipmentId())
        );
    }

    private void validatePagination(Integer pageSize, Integer pageNumber) {
        if (pageNumber != null && pageNumber < DEFAULT_PAGE_NUMBER) {
            throw new ValueTooSmallException("Page number", DEFAULT_PAGE_NUMBER);
        }
        if (pageSize != null && pageSize < MIN_PAGE_SIZE) {
            throw new ValueTooSmallException("Page size", MIN_PAGE_SIZE);
        }
        if (pageSize != null && pageSize > MAX_PAGE_SIZE) {
            throw new ValueTooLargeException("Page size", MAX_PAGE_SIZE);
        }
    }

    private Priority validateAndMapPriority(String priority) {
        if (priority == null || priority.isBlank()) {
            throw new RequiredFieldException("Priority");
        }
        try {
            return Priority.valueOf(priority.trim());
        } catch (IllegalArgumentException exception) {
            throw new InvalidFieldValueException("Priority");
        }
    }

    private ServiceRequestStatus validateAndMapStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new RequiredFieldException("Status");
        }
        try {
            return ServiceRequestStatus.valueOf(status.trim());
        } catch (IllegalArgumentException exception) {
            throw new InvalidFieldValueException("Status");
        }
    }

    private UUID validateAndMapId(String id, String fieldName) {
        if (id == null || id.isEmpty()) {
            throw new RequiredFieldException(fieldName);
        }
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException exception) {
            throw new InvalidIdException(fieldName);
        }
    }

}
