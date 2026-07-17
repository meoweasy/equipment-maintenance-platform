package com.example.maintenance.repository;

import com.example.maintenance.entity.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ServiceRequestRepository extends
        JpaRepository<ServiceRequest, UUID>,
        JpaSpecificationExecutor<ServiceRequest> {
}
