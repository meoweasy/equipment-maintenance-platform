package com.example.maintenance.client;

import com.example.maintenance.dto.ServiceRequestResponse.EquipmentResponse;
import com.example.platform.common.exception.ResourceNotFoundException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.UUID;

@Component
public class EquipmentClient {

    private final RestClient restClient;
    private final Cache<UUID, EquipmentResponse> cache;

    public EquipmentClient(
            RestClient.Builder restClientBuilder,
            @Value("${services.equipment.base-url}") String equipmentServiceBaseUrl,
            @Value("${services.equipment.cache-ttl:30s}") Duration cacheTtl
    ) {
        this.restClient = restClientBuilder.baseUrl(equipmentServiceBaseUrl).build();
        this.cache = Caffeine.newBuilder()
                .maximumSize(1_000)
                .expireAfterWrite(cacheTtl)
                .build();
    }

    public EquipmentResponse getFresh(UUID equipmentId) {
        EquipmentResponse equipment = fetch(equipmentId);
        cache.put(equipmentId, equipment);
        return equipment;
    }

    public EquipmentResponse getCached(UUID equipmentId) {
        return cache.get(equipmentId, this::fetch);
    }

    private EquipmentResponse fetch(UUID equipmentId) {
        try {
            EquipmentResponse equipment = restClient.get()
                    .uri("/internal/equipment/{id}", equipmentId)
                    .retrieve()
                    .body(EquipmentResponse.class);
            if (equipment == null) {
                throw new IllegalStateException("Equipment service returned an empty response");
            }
            return equipment;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ResourceNotFoundException("Equipment", equipmentId);
            }
            throw exception;
        }
    }
}
