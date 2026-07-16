package com.example.maintenance.repository;

import com.example.maintenance.entity.ServiceRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ServiceRequestRepository extends CrudRepository<ServiceRequest, UUID> {

}
