package com.nookit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * CORS 配置项持有者。实际的 {@code CorsConfigurationSource} Bean 由 {@link SecurityConfig} 创建，
 * 以便与 Spring Security 共用同一份配置。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "nookit.cors")
public class CorsConfig {

    private List<String> allowedOrigins = List.of("*");
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
    private List<String> allowedHeaders = List.of("*");
    private boolean allowCredentials = true;
    private long maxAge = 3600L;
}
