package com.example.equipment.client;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.UUID;

@Component
public class MaintenanceClient {

    private final RestClient restClient;
    private final Cache<UUID, Boolean> activeRequestsCache;

    public MaintenanceClient(
            RestClient.Builder restClientBuilder,
            @Value("${services.maintenance.base-url}") String maintenanceServiceBaseUrl,
            @Value("${services.maintenance.cache-ttl:5s}") Duration cacheTtl
    ) {
        this.restClient = restClientBuilder.baseUrl(maintenanceServiceBaseUrl).build();
        this.activeRequestsCache = Caffeine.newBuilder()
                .maximumSize(1_000)
                .expireAfterWrite(cacheTtl)
                .build();
    }

    public boolean hasActiveRequest(UUID equipmentId) {
        Boolean cachedActive = activeRequestsCache.getIfPresent(equipmentId);
        if (Boolean.TRUE.equals(cachedActive)) {
            return true;
        }

        ActiveServiceRequestResponse response = restClient.get()
                .uri("/internal/service-requests/active?equipmentId={equipmentId}", equipmentId)
                .retrieve()
                .body(ActiveServiceRequestResponse.class);
        if (response == null) {
            throw new IllegalStateException("Maintenance service returned an empty response");
        }
        if (!equipmentId.equals(response.equipmentId())) {
            throw new IllegalStateException("Maintenance service returned a response for another equipment");
        }

        if (response.active()) {
            activeRequestsCache.put(equipmentId, true);
        } else {
            activeRequestsCache.invalidate(equipmentId);
        }
        return response.active();
    }

    private record ActiveServiceRequestResponse(UUID equipmentId, boolean active) {
    }
}