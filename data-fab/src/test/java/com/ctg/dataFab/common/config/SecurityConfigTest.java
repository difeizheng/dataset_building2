package com.ctg.dataFab.common.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SecurityConfig 单元测试
 *
 * @author Developer
 * @since 2026-07-01
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("安全配置测试")
class SecurityConfigTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("测试密码编码器存在")
    void testPasswordEncoderExists() {
        assertNotNull(passwordEncoder, "密码编码器应该存在");
    }

    @Test
    @DisplayName("测试密码编码")
    void testPasswordEncoding() {
        String rawPassword = "testPassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertNotNull(encodedPassword, "编码后的密码不应为空");
        assertNotEquals(rawPassword, encodedPassword, "编码后的密码应与原始密码不同");
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword), "密码匹配应该成功");
    }

    @Test
    @DisplayName("测试密码不匹配")
    void testPasswordNotMatch() {
        String rawPassword = "testPassword123";
        String wrongPassword = "wrongPassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertFalse(passwordEncoder.matches(wrongPassword, encodedPassword), "错误密码匹配应该失败");
    }
}
