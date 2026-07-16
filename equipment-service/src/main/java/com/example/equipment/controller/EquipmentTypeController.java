package com.example.equipment.controller;

import com.example.equipment.dto.EquipmentTypeCreateRequest;
import com.example.equipment.dto.EquipmentTypeListFilter;
import com.example.equipment.dto.EquipmentTypeResponse;
import com.example.platform.common.pagination.PageDto;
import com.example.equipment.services.EquipmentTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("${application.api-path}/equipment-types")
@RequiredArgsConstructor
public class EquipmentTypeController {

    private final EquipmentTypeService equipmentTypeService;

    @PostMapping
    public ResponseEntity<EquipmentTypeResponse> create(@RequestBody EquipmentTypeCreateRequest request) {
        EquipmentTypeResponse response = equipmentTypeService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).eTag(response.etag()).body(response);
    }

    @GetMapping
    public ResponseEntity<PageDto<EquipmentTypeResponse>> list(
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Integer pageNumber,
            @ModelAttribute EquipmentTypeListFilter filter
    ) {
        return ResponseEntity.ok(equipmentTypeService.list(filter, pageSize, pageNumber));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipmentTypeResponse> getById(@PathVariable String id) {
        return withEtag(equipmentTypeService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipmentTypeResponse> update(
            @PathVariable String id,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String etag,
            @RequestBody EquipmentTypeCreateRequest request
    ) {
        return withEtag(equipmentTypeService.update(id, etag, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String etag
    ) {
        equipmentTypeService.delete(id, etag);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<EquipmentTypeResponse> withEtag(EquipmentTypeResponse response) {
        return ResponseEntity.ok().eTag(response.etag()).body(response);
    }
}
