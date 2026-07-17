package com.example.maintenance.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MaintenanceOpenApiConfiguration {

    @Bean
    public OpenAPI maintenanceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Maintenance Service API")
                        .version("1.0.0")
                        .description("Manages equipment service requests, filtering and status transitions. "
                                + "A request is created in NEW status; DONE requests are immutable.")
                        .contact(new Contact().name("Equipment Maintenance Platform"))
                        .license(new License().name("Internal project")))
                .externalDocs(new ExternalDocumentation()
                        .description("OpenAPI JSON")
                        .url("/v3/api-docs"));
    }

    @Bean
    public GroupedOpenApi maintenancePublicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .displayName("Public Maintenance API")
                .pathsToMatch("/api/**")
                .build();
    }

    @Bean
    public GroupedOpenApi maintenanceInternalApi() {
        return GroupedOpenApi.builder()
                .group("internal")
                .displayName("Internal Maintenance API")
                .pathsToMatch("/internal/**")
                .build();
    }
}