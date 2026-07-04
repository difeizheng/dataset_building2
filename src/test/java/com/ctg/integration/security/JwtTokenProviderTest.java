package com.ctg.integration.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * JWT Token提供者测试
 *
 * @author CTG
 * @since 2026-07-01
 */
class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        String secret = "test-secret-key-for-jwt-signing-must-be-at-least-256-bits";
        long jwtExpiration = 3600000; // 1小时
        long refreshExpiration = 604800000; // 7天
        tokenProvider = new JwtTokenProvider(secret, jwtExpiration, refreshExpiration);
    }

    @Test
    void testGenerateToken() {
        String username = "testuser";
        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");

        String token = tokenProvider.generateToken(username, roles);
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void testGenerateRefreshToken() {
        String username = "testuser";

        String token = tokenProvider.generateRefreshToken(username);
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void testGetUsernameFromToken() {
        String username = "testuser";
        List<String> roles = List.of("ROLE_USER");

        String token = tokenProvider.generateToken(username, roles);
        String extractedUsername = tokenProvider.getUsernameFromToken(token);

        assertEquals(username, extractedUsername);
    }

    @Test
    void testGetRolesFromToken() {
        String username = "testuser";
        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");

        String token = tokenProvider.generateToken(username, roles);
        List<String> extractedRoles = tokenProvider.getRolesFromToken(token);

        assertEquals(roles, extractedRoles);
    }

    @Test
    void testValidateToken() {
        String username = "testuser";
        List<String> roles = List.of("ROLE_USER");

        String token = tokenProvider.generateToken(username, roles);
        boolean isValid = tokenProvider.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void testValidateInvalidToken() {
        boolean isValid = tokenProvider.validateToken("invalid-token");
        assertFalse(isValid);
    }
}
