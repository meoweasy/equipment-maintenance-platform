package com.example.equipment.controller;

import com.example.equipment.dto.EquipmentTypeCreateRequest;
import com.example.equipment.dto.EquipmentTypeResponse;
import com.example.equipment.services.EquipmentTypeService;
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
@RequestMapping("${application.api-path}/equipment-types")
@RequiredArgsConstructor
public class EquipmentTypeController {

    private final EquipmentTypeService equipmentTypeService;

    @PostMapping
    public ResponseEntity<EquipmentTypeResponse> create(@Valid @RequestBody EquipmentTypeCreateRequest request) {
        EquipmentTypeResponse response = equipmentTypeService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).eTag(response.etag()).body(response);
    }

    @GetMapping
    public ResponseEntity<PageDto<EquipmentTypeResponse>> list(
            @RequestParam(required = false)
            @Min(value = MIN_PAGE_SIZE, message = "Page size must not be less than 1")
            @Max(value = MAX_PAGE_SIZE, message = "Page size must not be greater than 20")
            Integer pageSize,
            @RequestParam(required = false)
            @Min(value = DEFAULT_PAGE_NUMBER, message = "Page number must not be less than 0")
            Integer pageNumber
    ) {
        return ResponseEntity.ok(equipmentTypeService.list(pageSize, pageNumber));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipmentTypeResponse> getById(@PathVariable UUID id) {
        return withEtag(equipmentTypeService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipmentTypeResponse> update(
            @PathVariable UUID id,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String etag,
            @Valid @RequestBody EquipmentTypeCreateRequest request
    ) {
        return withEtag(equipmentTypeService.update(id, etag, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String etag
    ) {
        equipmentTypeService.delete(id, etag);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<EquipmentTypeResponse> withEtag(EquipmentTypeResponse response) {
        return ResponseEntity.ok().eTag(response.etag()).body(response);
    }
}
