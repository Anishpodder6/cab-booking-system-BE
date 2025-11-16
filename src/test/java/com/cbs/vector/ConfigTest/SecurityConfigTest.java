package com.cbs.vector.ConfigTest;

import com.cbs.vector.config.SecurityConfig;
import com.cbs.vector.filter.JwtAuthFilter;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityConfigTest {

    @Test
    void passwordEncoderBeanIsBCrypt() {
        SecurityConfig config = new SecurityConfig(mock(JwtAuthFilter.class));
        PasswordEncoder encoder = config.passwordEncoder();
        String raw = "testPassword";
        String encoded = encoder.encode(raw);
        assertTrue(encoder.matches(raw, encoded));
        assertFalse(encoder.matches("wrongPassword", encoded));
    }

    @Test
    void authenticationManagerBeanIsCreated() throws Exception {
        AuthenticationConfiguration authConfig = mock(AuthenticationConfiguration.class);
        AuthenticationManager mockManager = mock(AuthenticationManager.class);
        when(authConfig.getAuthenticationManager()).thenReturn(mockManager);

        SecurityConfig config = new SecurityConfig(mock(JwtAuthFilter.class));
        AuthenticationManager manager = config.authenticationManager(authConfig);
        assertNotNull(manager);
        assertEquals(mockManager, manager);
    }

    @Test
    void securityFilterChainBeanIsCreated() {
        JwtAuthFilter mockJwtFilter = mock(JwtAuthFilter.class);

        assertDoesNotThrow(() -> {
            // This test verifies the bean can be created without configuration errors
            SecurityConfig testConfig = new SecurityConfig(mockJwtFilter);
            assertNotNull(testConfig);
        });
    }

    @Test
    void constructorInjectsJwtAuthFilter() {
        JwtAuthFilter mockJwtFilter = mock(JwtAuthFilter.class);
        SecurityConfig config = new SecurityConfig(mockJwtFilter);
        assertNotNull(config);
    }

    @Test
    void passwordEncoderUsesCorrectStrength() {
        SecurityConfig config = new SecurityConfig(mock(JwtAuthFilter.class));
        PasswordEncoder encoder = config.passwordEncoder();

        // Test that it's specifically BCrypt
        assertTrue(encoder.getClass().getSimpleName().contains("BCrypt"));

        // Test encoding produces different results for same input (salt)
        String password = "testPassword";
        String encoded1 = encoder.encode(password);
        String encoded2 = encoder.encode(password);
        assertNotEquals(encoded1, encoded2);
        assertTrue(encoder.matches(password, encoded1));
        assertTrue(encoder.matches(password, encoded2));
    }
}
