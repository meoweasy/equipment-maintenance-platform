package com.example.equipment.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class EquipmentOpenApiConfiguration {

    @Bean
    public OpenAPI equipmentOpenApi() {
        return new OpenAPI()
                .servers(List.of(new Server().url("/").description("API Gateway")))
                .info(new Info()
                        .title("API сервиса оборудования")
                        .version("1.0.0")
                        .description("Управление оборудованием и типами оборудования. Для изменения и удаления передавайте актуальный ETag в If-Match.")
                        .contact(new Contact().name("Платформа обслуживания оборудования"))
                        .license(new License().name("Учебный проект")))
                .externalDocs(new ExternalDocumentation()
                        .description("Спецификация OpenAPI в JSON")
                        .url("/v3/api-docs"));
    }

    @Bean
    public GroupedOpenApi equipmentPublicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .displayName("Публичное API оборудования")
                .pathsToMatch("/api/**")
                .build();
    }

    @Bean
    public GroupedOpenApi equipmentInternalApi() {
        return GroupedOpenApi.builder()
                .group("internal")
                .displayName("Внутреннее API оборудования")
                .pathsToMatch("/internal/**")
                .build();
    }
}
