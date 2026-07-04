package com.ctg.aiFab.gateway.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtService单元测试
 *
 * @author Developer
 * @since 2026-07-01
 */
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // 使用反射设置私有字段（简化测试）
        try {
            var secretField = JwtService.class.getDeclaredField("secret");
            secretField.setAccessible(true);
            secretField.set(jwtService, "test-secret-key-for-unit-testing-only");

            var expirationField = JwtService.class.getDeclaredField("expiration");
            expirationField.setAccessible(true);
            expirationField.set(jwtService, 3600000L);
        } catch (Exception e) {
            fail("Failed to setup test: " + e.getMessage());
        }
    }

    @Test
    void testGenerateToken() {
        String token = jwtService.generateToken(1L, "testuser");
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void testIsTokenValid() {
        String token = jwtService.generateToken(1L, "testuser");
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void testGetUserIdFromToken() {
        String token = jwtService.generateToken(123L, "testuser");
        Long userId = jwtService.getUserIdFromToken(token);
        assertEquals(123L, userId);
    }

    @Test
    void testGetUsernameFromToken() {
        String token = jwtService.generateToken(1L, "testuser");
        String username = jwtService.getUsernameFromToken(token);
        assertEquals("testuser", username);
    }

    @Test
    void testInvalidToken() {
        assertFalse(jwtService.isTokenValid("invalid-token"));
    }
}
