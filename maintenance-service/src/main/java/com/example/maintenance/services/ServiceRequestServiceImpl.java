package com.example.maintenance.services;

import com.example.maintenance.client.EquipmentClient;
import com.example.maintenance.dto.ServiceRequestCreateRequest;
import com.example.maintenance.dto.ServiceRequestListFilter;
import com.example.maintenance.dto.ServiceRequestResponse;
import com.example.maintenance.dto.ServiceRequestResponse.EquipmentResponse;
import com.example.maintenance.entity.ServiceRequest;
import com.example.maintenance.enums.ServiceRequestStatus;
import com.example.maintenance.mapper.ServiceRequestMapper;
import com.example.maintenance.repository.ServiceRequestRepository;
import com.example.platform.common.exception.PreconditionFailedException;
import com.example.platform.common.exception.ResourceNotFoundException;
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

import static com.example.platform.common.pagination.PaginationConstants.*;

@Service
@RequiredArgsConstructor
public class ServiceRequestServiceImpl implements ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final EquipmentClient equipmentClient;
    private final ServiceRequestMapper serviceRequestMapper;

    @Override
    @Transactional
    public ServiceRequestResponse create(ServiceRequestCreateRequest request) {
        EquipmentResponse equipment = equipmentClient.getFresh(request.equipmentId());
        if ("DECOMMISSIONED".equals(equipment.status())) {
            throw new PreconditionFailedException(
                    "Service request cannot be created for decommissioned equipment"
            );
        }

        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setEquipmentId(request.equipmentId());
        serviceRequest.setTitle(request.title().trim());
        String description = request.description() == null ? null : request.description().trim();
        serviceRequest.setDescription(description == null || description.isEmpty() ? null : description);
        serviceRequest.setPriority(request.priority());
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
        ServiceRequestListFilter effectiveFilter = filter == null
                ? new ServiceRequestListFilter(null, null, null)
                : filter;
        Pageable pageable = PageRequest.of(
                pageNumber == null ? DEFAULT_PAGE_NUMBER : pageNumber,
                pageSize == null ? DEFAULT_PAGE_SIZE : pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Specification<ServiceRequest> specification = (root, query, builder) -> builder.conjunction();
        if (effectiveFilter.status() != null) {
            specification = specification.and((root, query, builder) -> builder.equal(root.get("status"), effectiveFilter.status()));
        }
        if (effectiveFilter.priority() != null) {
            specification = specification.and((root, query, builder) -> builder.equal(root.get("priority"), effectiveFilter.priority()));
        }
        if (effectiveFilter.equipmentId() != null) {
            specification = specification.and((root, query, builder) -> builder.equal(root.get("equipmentId"), effectiveFilter.equipmentId()));
        }

        Page<ServiceRequest> serviceRequests = serviceRequestRepository.findAll(specification, pageable);
        return PageDto.of(serviceRequests, serviceRequest -> serviceRequestMapper.toResponse(
                serviceRequest,
                equipmentClient.getCached(serviceRequest.getEquipmentId())
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceRequestResponse getById(UUID id) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service request", id));
        return serviceRequestMapper.toResponse(
                serviceRequest,
                equipmentClient.getCached(serviceRequest.getEquipmentId())
        );
    }

    @Override
    @Transactional
    public ServiceRequestResponse update(UUID id, ServiceRequestCreateRequest request) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service request", id));
        if (serviceRequest.getStatus() == ServiceRequestStatus.DONE) {
            throw new PreconditionFailedException("Completed service request cannot be modified");
        }

        EquipmentResponse equipment = equipmentClient.getFresh(request.equipmentId());
        if ("DECOMMISSIONED".equals(equipment.status())) {
            throw new PreconditionFailedException(
                    "Service request cannot be created for decommissioned equipment"
            );
        }

        serviceRequest.setEquipmentId(request.equipmentId());
        serviceRequest.setTitle(request.title().trim());
        String description = request.description() == null ? null : request.description().trim();
        serviceRequest.setDescription(description == null || description.isEmpty() ? null : description);
        serviceRequest.setPriority(request.priority());

        return serviceRequestMapper.toResponse(serviceRequestRepository.saveAndFlush(serviceRequest), equipment);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service request", id));
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
    public ServiceRequestResponse changeStatus(UUID id, ServiceRequestStatus status) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service request", id));
        ServiceRequestStatus currentStatus = serviceRequest.getStatus();

        if (serviceRequest.getStatus() == ServiceRequestStatus.DONE) {
            throw new PreconditionFailedException("Completed service request cannot be modified");
        }
        if (currentStatus == ServiceRequestStatus.CANCELLED
                && status == ServiceRequestStatus.IN_PROGRESS) {
            throw new PreconditionFailedException(
                    "Cancelled service request cannot be moved to in progress"
            );
        }

        serviceRequest.setStatus(status);
        if (status == ServiceRequestStatus.DONE) {
            serviceRequest.setCompletedAt(LocalDate.now());
        }

        ServiceRequest savedServiceRequest = serviceRequestRepository.saveAndFlush(serviceRequest);
        return serviceRequestMapper.toResponse(
                savedServiceRequest,
                equipmentClient.getCached(savedServiceRequest.getEquipmentId())
        );
    }
}
