package com.example.maintenance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@SpringBootApplication
public class MaintenanceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaintenanceServiceApplication.class, args);
    }

    @Bean
    RestClient equipmentRestClient(
            RestClient.Builder builder,
            org.springframework.beans.factory.annotation.Value("${services.equipment.base-url}")
            String baseUrl
    ) {
        return builder.baseUrl(baseUrl).build();
    }
}
