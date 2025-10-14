package com.ec.library.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "EContract Microservices API",
                version = "1.0.0",
                description = "Centralized Swagger configuration for all microservices"
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local Gateway")
        }
)
public class SwaggerConfig {
    // Có thể thêm custom bean nếu cần
}
