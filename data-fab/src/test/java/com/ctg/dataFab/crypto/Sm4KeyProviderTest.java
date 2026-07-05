package com.ctg.dataFab.crypto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SM4 密钥提供者测试
 *
 * @author Security Officer
 * @since 2026-07-05
 */
@DisplayName("SM4 密钥提供者测试")
class Sm4KeyProviderTest {

    @Test
    @DisplayName("未配置密钥时抛出异常")
    void getKey_notConfigured_throwsException() {
        EnvironmentSm4KeyProvider provider = new EnvironmentSm4KeyProvider();
        setField(provider, "encryptionKey", "");

        assertThatThrownBy(provider::getKey)
            .isInstanceOf(Sm4EncryptionException.class)
            .hasMessageContaining("not configured");
    }

    @Test
    @DisplayName("密钥长度非16字节时抛出异常")
    void getKey_invalidLength_throwsException() {
        EnvironmentSm4KeyProvider provider = new EnvironmentSm4KeyProvider();
        // 设置 15 字节密钥
        String shortKey = Base64.getEncoder().encodeToString("123456789012345".getBytes());
        setField(provider, "encryptionKey", shortKey);

        assertThatThrownBy(provider::getKey)
            .isInstanceOf(Sm4EncryptionException.class)
            .hasMessageContaining("16 bytes");
    }

    @Test
    @DisplayName("有效密钥返回正确长度")
    void getKey_validKey_returns16Bytes() {
        // 16 字节密钥 Base64 编码
        byte[] key16Bytes = "1234567890123456".getBytes();
        String validKey = Base64.getEncoder().encodeToString(key16Bytes);

        EnvironmentSm4KeyProvider provider = new EnvironmentSm4KeyProvider();
        setField(provider, "encryptionKey", validKey);

        byte[] result = provider.getKey();
        assertThat(result).hasSize(16);
    }

    @Test
    @DisplayName("非 Base64 格式时抛出异常")
    void getKey_invalidBase64_throwsException() {
        EnvironmentSm4KeyProvider provider = new EnvironmentSm4KeyProvider();
        setField(provider, "encryptionKey", "not-valid-base64!!!");

        assertThatThrownBy(provider::getKey)
            .isInstanceOf(Sm4EncryptionException.class)
            .hasMessageContaining("Base64");
    }

    private void setField(Object target, String fieldName, String value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
