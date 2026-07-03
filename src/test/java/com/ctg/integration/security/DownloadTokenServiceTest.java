package com.ctg.integration.security;

import com.ctg.integration.crypto.SMCryptoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DownloadTokenService 单元测试
 * 验证下载令牌的生成和验证逻辑
 *
 * @author CTG
 * @since 2026-07-03
 */
@DisplayName("下载令牌服务测试")
class DownloadTokenServiceTest {

    private SMCryptoUtils smCryptoUtils;
    private DownloadTokenService downloadTokenService;

    @BeforeEach
    void setUp() {
        smCryptoUtils = new SMCryptoUtils();
        smCryptoUtils.init();
        downloadTokenService = new DownloadTokenService(smCryptoUtils);
    }

    @Test
    @DisplayName("生成下载令牌 - 成功")
    void generateDownloadToken_success() {
        String token = downloadTokenService.generateDownloadToken(1L, "resource-123", 30);

        assertNotNull(token);
        assertTrue(token.contains("."));
        // 令牌应包含两部分：内容和签名
        String[] parts = token.split("\\.");
        assertEquals(2, parts.length);
    }

    @Test
    @DisplayName("验证下载令牌 - 有效令牌")
    void validateDownloadToken_valid() {
        Long userId = 1L;
        String resourceId = "resource-123";
        String token = downloadTokenService.generateDownloadToken(userId, resourceId, 30);

        boolean valid = downloadTokenService.validateDownloadToken(token, userId, resourceId);

        assertTrue(valid);
    }

    @Test
    @DisplayName("验证下载令牌 - 用户不匹配")
    void validateDownloadToken_userMismatch() {
        Long userId = 1L;
        String resourceId = "resource-123";
        String token = downloadTokenService.generateDownloadToken(userId, resourceId, 30);

        // 使用不同的用户ID验证
        boolean valid = downloadTokenService.validateDownloadToken(token, 2L, resourceId);

        assertFalse(valid);
    }

    @Test
    @DisplayName("验证下载令牌 - 资源不匹配")
    void validateDownloadToken_resourceMismatch() {
        Long userId = 1L;
        String resourceId = "resource-123";
        String token = downloadTokenService.generateDownloadToken(userId, resourceId, 30);

        // 使用不同的资源ID验证
        boolean valid = downloadTokenService.validateDownloadToken(token, userId, "resource-456");

        assertFalse(valid);
    }

    @Test
    @DisplayName("验证下载令牌 - 已过期")
    void validateDownloadToken_expired() {
        Long userId = 1L;
        String resourceId = "resource-123";
        // 生成一个立即过期的令牌（0分钟有效期）
        String token = downloadTokenService.generateDownloadToken(userId, resourceId, 0);

        // 等待1秒确保过期
        try { Thread.sleep(1100); } catch (InterruptedException ignored) {}

        boolean valid = downloadTokenService.validateDownloadToken(token, userId, resourceId);

        assertFalse(valid);
    }

    @Test
    @DisplayName("验证下载令牌 - 格式错误")
    void validateDownloadToken_malformed() {
        boolean valid = downloadTokenService.validateDownloadToken("invalid-token", 1L, "resource-123");

        assertFalse(valid);
    }

    @Test
    @DisplayName("验证下载令牌 - 签名被篡改")
    void validateDownloadToken_tamperedSignature() {
        Long userId = 1L;
        String resourceId = "resource-123";
        String token = downloadTokenService.generateDownloadToken(userId, resourceId, 30);

        // 篡改签名
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";

        boolean valid = downloadTokenService.validateDownloadToken(tamperedToken, userId, resourceId);

        assertFalse(valid);
    }

    @Test
    @DisplayName("获取令牌过期时间 - 成功")
    void getTokenExpiration_success() {
        String token = downloadTokenService.generateDownloadToken(1L, "resource-123", 30);

        Long expiration = downloadTokenService.getTokenExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration > System.currentTimeMillis() / 1000);
    }

    @Test
    @DisplayName("获取令牌过期时间 - 无效令牌")
    void getTokenExpiration_invalid() {
        Long expiration = downloadTokenService.getTokenExpiration("invalid");

        assertNull(expiration);
    }
}
