package com.example.equipment.controller;

import com.example.equipment.dto.EquipmentCreateRequest;
import com.example.equipment.dto.EquipmentListFilter;
import com.example.equipment.dto.EquipmentResponse;
import com.example.equipment.enums.EquipmentStatus;
import com.example.equipment.services.EquipmentService;
import com.example.platform.common.pagination.PageDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

import static com.example.platform.common.pagination.PaginationConstants.DEFAULT_PAGE_NUMBER;
import static com.example.platform.common.pagination.PaginationConstants.MAX_PAGE_SIZE;
import static com.example.platform.common.pagination.PaginationConstants.MIN_PAGE_SIZE;

@RestController
@RequiredArgsConstructor
public class EquipmentController {

    private static final String API_PATH = "${application.api-path}/equipment";
    private static final String INTERNAL_PATH = "${application.internal-path}/equipment";

    private final EquipmentService equipmentService;

    @PostMapping(API_PATH)
    public ResponseEntity<EquipmentResponse> create(@Valid @RequestBody EquipmentCreateRequest request) {
        EquipmentResponse response = equipmentService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).eTag(response.etag()).body(response);
    }

    @GetMapping(API_PATH)
    public ResponseEntity<PageDto<EquipmentResponse>> list(
            @RequestParam(required = false)
            @Min(value = MIN_PAGE_SIZE, message = "Page size must not be less than 1")
            @Max(value = MAX_PAGE_SIZE, message = "Page size must not be greater than 20")
            Integer pageSize,
            @RequestParam(required = false)
            @Min(value = DEFAULT_PAGE_NUMBER, message = "Page number must not be less than 0")
            Integer pageNumber,
            @ModelAttribute EquipmentListFilter filter
    ) {
        return ResponseEntity.ok(equipmentService.list(filter, pageSize, pageNumber));
    }

    @GetMapping(API_PATH + "/{id}")
    public ResponseEntity<EquipmentResponse> getById(@PathVariable UUID id) {
        return withEtag(equipmentService.getById(id));
    }

    @PutMapping(API_PATH + "/{id}")
    public ResponseEntity<EquipmentResponse> update(
            @PathVariable UUID id,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String etag,
            @Valid @RequestBody EquipmentCreateRequest request
    ) {
        return withEtag(equipmentService.update(id, etag, request));
    }

    @DeleteMapping(API_PATH + "/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String etag
    ) {
        equipmentService.delete(id, etag);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(INTERNAL_PATH + "/{id}")
    public ResponseEntity<EquipmentResponse> getInternalById(@PathVariable UUID id) {
        return withEtag(equipmentService.getById(id));
    }

    @PatchMapping(INTERNAL_PATH + "/{id}/status")
    public ResponseEntity<EquipmentResponse> changeStatus(
            @PathVariable UUID id,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String etag,
            @RequestParam EquipmentStatus status
    ) {
        return withEtag(equipmentService.changeStatus(id, etag, status));
    }

    private ResponseEntity<EquipmentResponse> withEtag(EquipmentResponse response) {
        return ResponseEntity.ok().eTag(response.etag()).body(response);
    }
}
