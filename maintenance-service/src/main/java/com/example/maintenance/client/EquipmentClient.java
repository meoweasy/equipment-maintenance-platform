package com.example.maintenance.client;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class EquipmentClient {

    private final RestClient restClient;

    public EquipmentClient(RestClient equipmentRestClient) {
        this.restClient = equipmentRestClient;
    }

    public void ensureEquipmentExists(UUID equipmentId) {
        restClient.get()
                .uri("/api/v1/equipment/{id}", equipmentId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new IllegalArgumentException(
                            "Оборудование с id " + equipmentId + " не найдено"
                    );
                })
                .toBodilessEntity();
    }
}
