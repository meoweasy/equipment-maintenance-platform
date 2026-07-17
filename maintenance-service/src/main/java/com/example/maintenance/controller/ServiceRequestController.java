package com.example.maintenance.controller;

import com.example.maintenance.dto.ActiveServiceRequestResponse;
import com.example.maintenance.dto.ServiceRequestCreateRequest;
import com.example.maintenance.dto.ServiceRequestListFilter;
import com.example.maintenance.dto.ServiceRequestResponse;
import com.example.maintenance.enums.ServiceRequestStatus;
import com.example.maintenance.services.ServiceRequestService;
import com.example.platform.common.pagination.PageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

import static com.example.platform.common.pagination.PaginationConstants.DEFAULT_PAGE_NUMBER;
import static com.example.platform.common.pagination.PaginationConstants.MAX_PAGE_SIZE;
import static com.example.platform.common.pagination.PaginationConstants.MIN_PAGE_SIZE;

@RestController
@RequiredArgsConstructor
@Tag(name = "Service requests", description = "Maintenance requests and their lifecycle")
public class ServiceRequestController {

    private static final String API_PATH = "${application.api-path}/service-requests";
    private static final String INTERNAL_PATH = "${application.internal-path}/service-requests";

    private final ServiceRequestService serviceRequestService;

    @Operation(summary = "Create a service request", description = "Creates it in NEW status. Equipment must exist and must not be DECOMMISSIONED.")
    @PostMapping(API_PATH)
    public ResponseEntity<ServiceRequestResponse> create(
            @Valid @RequestBody ServiceRequestCreateRequest request
    ) {
        ServiceRequestResponse response = serviceRequestService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "List service requests", description = "Optional status, priority and equipmentId filters can be combined.")
    @GetMapping(API_PATH)
    public ResponseEntity<PageDto<ServiceRequestResponse>> list(
            @RequestParam(required = false)
            @Min(value = MIN_PAGE_SIZE, message = "Page size must not be less than 1")
            @Max(value = MAX_PAGE_SIZE, message = "Page size must not be greater than 20")
            Integer pageSize,
            @RequestParam(required = false)
            @Min(value = DEFAULT_PAGE_NUMBER, message = "Page number must not be less than 0")
            Integer pageNumber,
            @ModelAttribute ServiceRequestListFilter filter
    ) {
        return ResponseEntity.ok(serviceRequestService.list(filter, pageSize, pageNumber));
    }

    @Operation(summary = "Get a service request")
    @GetMapping(API_PATH + "/{id}")
    public ResponseEntity<ServiceRequestResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceRequestService.getById(id));
    }

    @Operation(summary = "Replace a service request", description = "A request in DONE status is read-only.")
    @PutMapping(API_PATH + "/{id}")
    public ResponseEntity<ServiceRequestResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody ServiceRequestCreateRequest request
    ) {
        return ResponseEntity.ok(serviceRequestService.update(id, request));
    }

    @Operation(summary = "Delete a service request", description = "IN_PROGRESS and DONE requests cannot be deleted.")
    @DeleteMapping(API_PATH + "/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        serviceRequestService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Change request status", description = "DONE sets completedAt automatically. DONE and CANCELLED cannot return to IN_PROGRESS.")
    @PatchMapping(API_PATH + "/{id}/status")
    public ResponseEntity<ServiceRequestResponse> changeStatus(
            @PathVariable UUID id,
            @RequestParam ServiceRequestStatus status
    ) {
        return ResponseEntity.ok(serviceRequestService.changeStatus(id, status));
    }

    @Operation(summary = "Check for active requests", description = "Internal endpoint used before equipment deletion. DONE and CANCELLED are not active.", tags = "Internal service request API")
    @GetMapping(INTERNAL_PATH + "/active")
    public ResponseEntity<ActiveServiceRequestResponse> hasActiveRequest(
            @RequestParam UUID equipmentId
    ) {
        boolean active = serviceRequestService.hasActiveRequest(equipmentId);
        return ResponseEntity.ok(new ActiveServiceRequestResponse(equipmentId, active));
    }
}