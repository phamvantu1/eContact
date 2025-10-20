package com.ec.library.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
                        .addServersItem(server);
        }
}
