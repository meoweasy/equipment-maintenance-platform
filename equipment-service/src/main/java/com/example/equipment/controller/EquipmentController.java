package com.example.equipment.controller;

import com.example.equipment.dto.EquipmentCreateRequest;
import com.example.equipment.dto.EquipmentListFilter;
import com.example.equipment.dto.EquipmentResponse;
import com.example.equipment.enums.EquipmentStatus;
import com.example.equipment.services.EquipmentService;
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
@Tag(name = "Оборудование", description = "Управление оборудованием, поиск и изменение статусов")
public class EquipmentController {

    private static final String API_PATH = "${application.api-path}/equipment";
    private static final String INTERNAL_PATH = "${application.internal-path}/equipment";

    private final EquipmentService equipmentService;

    @Operation(summary = "Создать оборудование", description = "Создаёт оборудование в статусе AVAILABLE. Тип должен существовать, инвентарный номер должен быть уникальным.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Оборудование создано"), @ApiResponse(responseCode = "400", description = "Некорректный запрос"), @ApiResponse(responseCode = "404", description = "Тип оборудования не найден"), @ApiResponse(responseCode = "409", description = "Инвентарный номер уже существует")})
    @PostMapping(API_PATH)
    public ResponseEntity<EquipmentResponse> create(@Valid @RequestBody EquipmentCreateRequest request) {
        EquipmentResponse response = equipmentService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).eTag(response.etag()).body(response);
    }

    @Operation(summary = "Получить список оборудования", description = "Возвращает страницу оборудования. Фильтры можно комбинировать.")
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

    @Operation(summary = "Получить оборудование", description = "Возвращает оборудование и актуальный ETag в заголовке ответа.")
    @GetMapping(API_PATH + "/{id}")
    public ResponseEntity<EquipmentResponse> getById(@PathVariable UUID id) {
        return withEtag(equipmentService.getById(id));
    }

    @Operation(summary = "Изменить оборудование", description = "Полностью заменяет данные. Передайте актуальный ETag в If-Match.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Оборудование изменено"), @ApiResponse(responseCode = "412", description = "ETag устарел"), @ApiResponse(responseCode = "428", description = "Заголовок If-Match обязателен")})
    @PutMapping(API_PATH + "/{id}")
    public ResponseEntity<EquipmentResponse> update(
            @PathVariable UUID id,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String etag,
            @Valid @RequestBody EquipmentCreateRequest request
    ) {
        return withEtag(equipmentService.update(id, etag, request));
    }

    @Operation(summary = "Удалить оборудование", description = "Разрешено только при отсутствии активных заявок. Требуется актуальный ETag.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Оборудование удалено"), @ApiResponse(responseCode = "412", description = "ETag устарел или существуют активные заявки"), @ApiResponse(responseCode = "428", description = "Заголовок If-Match обязателен")})
    @DeleteMapping(API_PATH + "/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String etag
    ) {
        equipmentService.delete(id, etag);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить оборудование for maintenance-service", description = "Внутренний метод проверки оборудования и наполнения ответа", tags = "Внутреннее API оборудования")
    @GetMapping(INTERNAL_PATH + "/{id}")
    public ResponseEntity<EquipmentResponse> getInternalById(@PathVariable UUID id) {
        return withEtag(equipmentService.getById(id));
    }

    @Operation(summary = "Изменить статус оборудования", description = "Внутренняя операция. Оборудование на обслуживании нельзя списать.", tags = "Внутреннее API оборудования")
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
