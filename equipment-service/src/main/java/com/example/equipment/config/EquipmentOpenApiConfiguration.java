package com.example.equipment.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EquipmentOpenApiConfiguration {

    @Bean
    public OpenAPI equipmentOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Equipment Service API")
                        .version("1.0.0")
                        .description("Manages equipment and equipment types. "
                                + "Update and delete operations use If-Match with the ETag returned by read operations.")
                        .contact(new Contact().name("Equipment Maintenance Platform"))
                        .license(new License().name("Internal project")))
                .externalDocs(new ExternalDocumentation()
                        .description("OpenAPI JSON")
                        .url("/v3/api-docs"));
    }

    @Bean
    public GroupedOpenApi equipmentPublicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .displayName("Public Equipment API")
                .pathsToMatch("/api/**")
                .build();
    }

    @Bean
    public GroupedOpenApi equipmentInternalApi() {
        return GroupedOpenApi.builder()
                .group("internal")
                .displayName("Internal Equipment API")
                .pathsToMatch("/internal/**")
                .build();
    }
}