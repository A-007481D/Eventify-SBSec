package com.eventify.config;

import com.eventify.security.CustomAccessDeniedHandler;
import com.eventify.security.CustomAuthenticationEntryPoint;
import com.eventify.security.CustomAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
@Profile("!test")
public class SecurityConfig {

    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationProvider authenticationProvider;

    private static final String[] PUBLIC_ENDPOINTS = {
        "/api/public/**",
        "/h2-console/**"
    };

    public SecurityConfig(CustomAuthenticationEntryPoint authenticationEntryPoint,
                         CustomAccessDeniedHandler accessDeniedHandler,
                         CustomAuthenticationProvider authenticationProvider) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Enable H2 console in a frame for development
        http.headers(headers -> headers
            .frameOptions(frame -> frame
                .sameOrigin()
            )
        );

        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/organizer/**").hasAnyRole("ORGANIZER", "ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> basic
                .authenticationEntryPoint(authenticationEntryPoint)
            )
            .authenticationProvider(authenticationProvider)
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws  Exception {
        return config.getAuthenticationManager();
    }

}