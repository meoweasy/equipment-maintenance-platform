package com.example.maintenance.service;

import java.util.UUID;

public class MaintenanceNotFoundException extends RuntimeException {

    public MaintenanceNotFoundException(UUID id) {
        super("Заявка на обслуживание с id " + id + " не найдена");
    }
}
