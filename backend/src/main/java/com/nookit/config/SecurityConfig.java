package com.nookit.config;

import com.nookit.common.api.Result;
import com.nookit.common.api.ResultCode;
import com.nookit.common.util.JsonUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Data
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@ConfigurationProperties(prefix = "nookit.security")
@RequiredArgsConstructor
public class SecurityConfig {

    private List<String> permitAll = List.of();

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CorsConfigurationSource corsSource) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(f -> f.disable())
                .httpBasic(b -> b.disable())
                .authorizeHttpRequests(reg -> reg
                        .requestMatchers(permitAll.toArray(new String[0])).permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint((req, resp, ex) -> writeJson(resp,
                                HttpServletResponse.SC_UNAUTHORIZED,
                                Result.error(ResultCode.UNAUTHORIZED)))
                        .accessDeniedHandler((req, resp, ex) -> writeJson(resp,
                                HttpServletResponse.SC_FORBIDDEN,
                                Result.error(ResultCode.FORBIDDEN)))
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsConfig corsConfig) {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        var conf = new org.springframework.web.cors.CorsConfiguration();
        if (corsConfig.isAllowCredentials()) {
            conf.setAllowedOriginPatterns(corsConfig.getAllowedOrigins());
        } else {
            conf.setAllowedOrigins(corsConfig.getAllowedOrigins());
        }
        conf.setAllowedMethods(corsConfig.getAllowedMethods());
        conf.setAllowedHeaders(corsConfig.getAllowedHeaders());
        conf.setAllowCredentials(corsConfig.isAllowCredentials());
        conf.setMaxAge(corsConfig.getMaxAge());
        source.registerCorsConfiguration("/**", conf);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    private void writeJson(HttpServletResponse response, int status, Result<?> body) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JsonUtil.toJson(body));
    }
}
