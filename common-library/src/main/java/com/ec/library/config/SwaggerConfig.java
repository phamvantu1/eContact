package com.ec.library.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;

@Configuration
public class SwaggerConfig {

        @Value("${server.port}")
        private String port;

        @Value("${server.servlet.context-path:}")
        private String contextPath;

        @Bean
        public OpenAPI customOpenAPI() {
                String url = "http://localhost:" + port + contextPath;
                Server server = new Server()
                        .url(url)
                        .description("Local environment");

                return new OpenAPI()
                        .info(new Info()
                                .title("EContract Microservices API")
                                .version("1.0.0")
                                .description("Centralized Swagger configuration for all microservices"))
                        .addServersItem(server)
                        .components(new Components()
                                .addSecuritySchemes("bearerAuth",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")))
                        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
        }
}
