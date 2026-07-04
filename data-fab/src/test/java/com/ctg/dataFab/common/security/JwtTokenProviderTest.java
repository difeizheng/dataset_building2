package com.ctg.dataFab.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT Token 提供者单元测试
 *
 * @author Developer
 * @since 2026-07-05
 */
@DisplayName("JWT Token 提供者测试")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        // 使用测试配置
        String secret = "test-secret-key-for-jwt-token-generation-minimum-32-bytes";
        long jwtExpiration = 3600000L; // 1小时
        long refreshExpiration = 86400000L; // 24小时

        jwtTokenProvider = new JwtTokenProvider(secret, jwtExpiration, refreshExpiration);
    }

    @Test
    @DisplayName("生成访问令牌 - 成功")
    void testGenerateToken_Success() {
        String username = "testuser";
        List<String> roles = Arrays.asList("USER", "ADMIN");

        String token = jwtTokenProvider.generateToken(username, roles);

        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    @DisplayName("从令牌获取用户名")
    void testGetUsernameFromToken() {
        String username = "testuser";
        List<String> roles = Arrays.asList("USER");
        String token = jwtTokenProvider.generateToken(username, roles);

        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("从令牌获取角色")
    void testGetRolesFromToken() {
        String username = "testuser";
        List<String> roles = Arrays.asList("USER", "ADMIN");
        String token = jwtTokenProvider.generateToken(username, roles);

        List<String> extractedRoles = jwtTokenProvider.getRolesFromToken(token);

        assertNotNull(extractedRoles);
        assertEquals(2, extractedRoles.size());
        assertTrue(extractedRoles.contains("USER"));
        assertTrue(extractedRoles.contains("ADMIN"));
    }

    @Test
    @DisplayName("验证有效令牌")
    void testValidateToken_Valid() {
        String username = "testuser";
        List<String> roles = Arrays.asList("USER");
        String token = jwtTokenProvider.generateToken(username, roles);

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("验证无效令牌")
    void testValidateToken_Invalid() {
        String invalidToken = "invalid-token-string";

        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("验证过期令牌")
    void testValidateToken_Expired() {
        // 创建一个已过期的token提供者
        String secret = "test-secret-key-for-jwt-token-generation-minimum-32-bytes";
        JwtTokenProvider expiredProvider = new JwtTokenProvider(secret, -1000L, 86400000L);

        String username = "testuser";
        List<String> roles = Arrays.asList("USER");
        String token = expiredProvider.generateToken(username, roles);

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("生成刷新令牌")
    void testGenerateRefreshToken() {
        String username = "testuser";

        String refreshToken = jwtTokenProvider.generateRefreshToken(username);

        assertNotNull(refreshToken);
        assertTrue(refreshToken.length() > 0);
    }

    @Test
    @DisplayName("从令牌获取认证信息")
    void testGetAuthentication() {
        String username = "testuser";
        List<String> roles = Arrays.asList("USER", "ADMIN");
        String token = jwtTokenProvider.generateToken(username, roles);

        Authentication authentication = jwtTokenProvider.getAuthentication(token);

        assertNotNull(authentication);
        assertTrue(authentication.getPrincipal() instanceof User);
        User principal = (User) authentication.getPrincipal();
        assertEquals(username, principal.getUsername());
        assertEquals(2, principal.getAuthorities().size());
    }

    @Test
    @DisplayName("短密钥自动填充到32字节")
    void testShortKeyPadding() {
        String shortSecret = "short";
        JwtTokenProvider provider = new JwtTokenProvider(shortSecret, 3600000L, 86400000L);

        String username = "testuser";
        List<String> roles = Arrays.asList("USER");
        String token = provider.generateToken(username, roles);

        assertNotNull(token);
        assertTrue(provider.validateToken(token));
    }
}
