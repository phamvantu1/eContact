package com.ec.gateway.config;

import com.ec.gateway.filter.JwtAuthenticationFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
//@Profile("local")
public class GatewayConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public GatewayConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth service: không cần JWT filter
                .route("auth-service", r -> r.path("/api/auth/**")
                        .uri("lb://EC-AUTH-SRV"))

                //  yêu cầu JWT filter
                .route("customer-service", r -> r.path("/api/customers/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://EC-CUSTOMER-SRV"))
                .route("notice-service", r -> r.path("/api/notifications/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://EC-NOTIFICATION-SRV"))
                .route("contract-service", r -> r.path("/api/contracts/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://EC-CONTRACT-SRV"))
                .build();
    }


}
