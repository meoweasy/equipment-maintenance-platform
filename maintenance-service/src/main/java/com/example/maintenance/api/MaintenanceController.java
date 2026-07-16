package com.example.maintenance.api;

import com.example.maintenance.domain.MaintenanceRequest;
import com.example.maintenance.domain.MaintenanceStatus;
import com.example.maintenance.service.MaintenanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/maintenance")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @GetMapping
    public List<MaintenanceRequest> findAll() {
        return maintenanceService.findAll();
    }

    @GetMapping("/{id}")
    public MaintenanceRequest findById(@PathVariable UUID id) {
        return maintenanceService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MaintenanceRequest create(
            @Valid @RequestBody CreateMaintenanceRequest request
    ) {
        return maintenanceService.create(request);
    }

    @PatchMapping("/{id}/status")
    public MaintenanceRequest updateStatus(
            @PathVariable UUID id,
            @RequestParam MaintenanceStatus status
    ) {
        return maintenanceService.updateStatus(id, status);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        maintenanceService.delete(id);
    }
}
