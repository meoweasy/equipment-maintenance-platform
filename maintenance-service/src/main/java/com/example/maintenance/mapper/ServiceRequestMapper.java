package com.example.maintenance.mapper;

import com.example.maintenance.dto.ServiceRequestResponse;
import com.example.maintenance.dto.ServiceRequestResponse.EquipmentResponse;
import com.example.maintenance.entity.ServiceRequest;
import org.springframework.stereotype.Component;

@Component
public class ServiceRequestMapper {

    public ServiceRequestResponse toResponse(
            ServiceRequest serviceRequest,
            EquipmentResponse equipment
    ) {
        return new ServiceRequestResponse(
                equipment,
                serviceRequest.getTitle(),
                serviceRequest.getDescription(),
                serviceRequest.getStatus(),
                serviceRequest.getPriority(),
                serviceRequest.getCompletedAt()
        );
    }
}