//package com.ec.gateway.config;
//
//import com.ec.gateway.filter.JwtAuthenticationFilter;
//import org.springframework.cloud.gateway.route.RouteLocator;
//import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Profile;
//
//@Configuration
////@Profile("local")
//public class GatewayConfig {
//
//    private final JwtAuthenticationFilter jwtAuthFilter;
//
//    public GatewayConfig(JwtAuthenticationFilter jwtAuthFilter) {
//        this.jwtAuthFilter = jwtAuthFilter;
//    }
//
//    @Bean
//    public RouteLocator routes(RouteLocatorBuilder builder) {
//        return builder.routes()
//                .route("user-login", r -> r.path("/api/users/auth/login")
//                        .uri("http://localhost:8082")) // Không dùng filter ở đây
//                .route("user-protected", r -> r.path("/api/users/**")
//                        .filters(f -> f.filter(jwtAuthFilter))
//                        .uri("http://localhost:8082"))
//                .route("question-protected", r -> r.path("/api/questions/**")
//                        .filters(f -> f.filter(jwtAuthFilter))
//                        .uri("http://localhost:8081"))
//                .route("config-protected", r -> r.path("/api/config/**")
//                        .filters(f -> f.filter(jwtAuthFilter))
//                        .uri("http://localhost:8083"))
//                .route("quiz-protected", r -> r.path("/api/quizzes/**")
//                        .filters(f -> f.filter(jwtAuthFilter))
//                        .uri("http://localhost:8084"))
//                .route("notification-protected", r -> r.path("/api/notifications/**")
//                        .filters(f -> f.filter(jwtAuthFilter))
//                        .uri("http://localhost:8085"))
//                .route("notification-websocket", r -> r.path("/ws/notification/**")
//                        .uri("ws://localhost:8085"))
//                .route("question-websocket", r -> r.path("/ws/question/**")
//                        .uri("ws://localhost:8081"))
//                .build();
//    }
//
//
//}
