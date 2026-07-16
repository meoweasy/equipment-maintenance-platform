package com.example.maintenance.services;

import com.example.platform.common.pagination.PageDto;
import com.example.maintenance.dto.ServiceRequestCreateRequest;
import com.example.maintenance.dto.ServiceRequestListFilter;
import com.example.maintenance.dto.ServiceRequestResponse;

public interface ServiceRequestService {

    ServiceRequestResponse create(ServiceRequestCreateRequest request);

    PageDto<ServiceRequestResponse> list(ServiceRequestListFilter filter);

    ServiceRequestResponse getById(String id);

    ServiceRequestResponse update(String id, ServiceRequestCreateRequest request);

    void delete(String id);

    ServiceRequestResponse changeStatus(String id, String status);
}
