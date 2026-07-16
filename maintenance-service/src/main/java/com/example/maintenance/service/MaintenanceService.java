package com.example.maintenance.service;

import com.example.maintenance.api.CreateMaintenanceRequest;
import com.example.maintenance.client.EquipmentClient;
import com.example.maintenance.domain.MaintenanceRequest;
import com.example.maintenance.domain.MaintenanceStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MaintenanceService {

    private final Map<UUID, MaintenanceRequest> storage = new ConcurrentHashMap<>();
    private final EquipmentClient equipmentClient;

    public MaintenanceService(EquipmentClient equipmentClient) {
        this.equipmentClient = equipmentClient;
    }

    public List<MaintenanceRequest> findAll() {
        return new ArrayList<>(storage.values());
    }

    public MaintenanceRequest findById(UUID id) {
        MaintenanceRequest request = storage.get(id);
        if (request == null) {
            throw new MaintenanceNotFoundException(id);
        }
        return request;
    }

    public MaintenanceRequest create(CreateMaintenanceRequest request) {
        equipmentClient.ensureEquipmentExists(request.equipmentId());

        UUID id = UUID.randomUUID();
        MaintenanceRequest maintenance = new MaintenanceRequest(
                id,
                request.equipmentId(),
                request.description(),
                request.scheduledDate(),
                MaintenanceStatus.PLANNED
        );
        storage.put(id, maintenance);
        return maintenance;
    }

    public MaintenanceRequest updateStatus(UUID id, MaintenanceStatus status) {
        MaintenanceRequest current = findById(id);
        MaintenanceRequest updated = new MaintenanceRequest(
                current.id(),
                current.equipmentId(),
                current.description(),
                current.scheduledDate(),
                status
        );
        storage.put(id, updated);
        return updated;
    }

    public void delete(UUID id) {
        if (storage.remove(id) == null) {
            throw new MaintenanceNotFoundException(id);
        }
    }
}
