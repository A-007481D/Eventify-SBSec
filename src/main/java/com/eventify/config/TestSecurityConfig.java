package com.eventify.config;

import com.eventify.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("test")
public class TestSecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public TestSecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Custom AuthenticationProvider for tests that always accepts any password.
     * This allows testing without needing to know the actual password.
     */
    @Bean
    @Primary
    public AuthenticationProvider testAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(testPasswordEncoder());
        return provider;
    }

    /**
     * Password encoder that accepts any password (ghir for testing)
     */
    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return rawPassword.toString();
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                // Always return true to bypass password validation in tests
                return true;
            }
        };
    }
}
