package com.example.maintenance.services;

import com.example.maintenance.dto.ServiceRequestCreateRequest;
import com.example.maintenance.dto.ServiceRequestListFilter;
import com.example.maintenance.dto.ServiceRequestResponse;
import com.example.maintenance.enums.ServiceRequestStatus;
import com.example.platform.common.pagination.PageDto;

import java.util.UUID;

public interface ServiceRequestService {

    ServiceRequestResponse create(ServiceRequestCreateRequest request);

    PageDto<ServiceRequestResponse> list(ServiceRequestListFilter filter, Integer pageSize,
                                         Integer pageNumber);

    ServiceRequestResponse getById(UUID id);

    ServiceRequestResponse update(UUID id, ServiceRequestCreateRequest request);

    void delete(UUID id);

    ServiceRequestResponse changeStatus(UUID id, ServiceRequestStatus status);

    boolean hasActiveRequest(UUID equipmentId);
}
