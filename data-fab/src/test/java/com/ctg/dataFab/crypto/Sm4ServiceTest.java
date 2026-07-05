package com.ctg.dataFab.crypto;

import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * SM4 加密服务单元测试
 *
 * @author Security Officer
 * @since 2026-07-05
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SM4 加密服务测试")
class Sm4ServiceTest {

    @Mock
    private Sm4KeyProvider keyProvider;

    private Sm4CbcServiceImpl sm4Service;

    /** 16 字节测试密钥 */
    private static final byte[] TEST_KEY = "1234567890123456".getBytes(StandardCharsets.UTF_8);

    @BeforeEach
    void setUp() {
        when(keyProvider.getKey()).thenReturn(TEST_KEY);
        sm4Service = new Sm4CbcServiceImpl(keyProvider);
    }

    @Test
    @DisplayName("加密解密往返 - 普通字符串")
    void encryptDecrypt_roundTrip_normalString() {
        String plainText = "Hello, SM4 Encryption!";
        String encrypted = sm4Service.encrypt(plainText);
        String decrypted = sm4Service.decrypt(encrypted);
        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    @DisplayName("加密解密往返 - 空字符串")
    void encryptDecrypt_roundTrip_emptyString() {
        String plainText = "";
        String encrypted = sm4Service.encrypt(plainText);
        String decrypted = sm4Service.decrypt(encrypted);
        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    @DisplayName("加密解密往返 - 长字符串")
    void encryptDecrypt_roundTrip_longString() {
        String plainText = "A".repeat(10000);
        String encrypted = sm4Service.encrypt(plainText);
        String decrypted = sm4Service.decrypt(encrypted);
        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    @DisplayName("加密解密往返 - 特殊字符")
    void encryptDecrypt_roundTrip_specialChars() {
        String plainText = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
        String encrypted = sm4Service.encrypt(plainText);
        String decrypted = sm4Service.decrypt(encrypted);
        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    @DisplayName("加密解密往返 - 中文内容")
    void encryptDecrypt_roundTrip_chineseChars() {
        String plainText = "国密SM4加密算法测试数据";
        String encrypted = sm4Service.encrypt(plainText);
        String decrypted = sm4Service.decrypt(encrypted);
        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    @DisplayName("加密返回值非明文")
    void encrypt_returnsCipherText_notPlainText() {
        String plainText = "secret data";
        String encrypted = sm4Service.encrypt(plainText);
        assertThat(encrypted).isNotEqualTo(plainText);
        // 密文应该是 Base64 编码的
        assertThat(encrypted).doesNotContain("secret");
    }

    @Test
    @DisplayName("相同明文每次加密结果不同（随机IV）")
    void encrypt_samePlainText_differentCipherText() {
        String plainText = "same content";
        String encrypted1 = sm4Service.encrypt(plainText);
        String encrypted2 = sm4Service.encrypt(plainText);
        // 由于随机 IV，每次加密结果不同
        assertThat(encrypted1).isNotEqualTo(encrypted2);
        // 但解密后应相同
        assertThat(sm4Service.decrypt(encrypted1)).isEqualTo(plainText);
        assertThat(sm4Service.decrypt(encrypted2)).isEqualTo(plainText);
    }

    @Test
    @DisplayName("null 输入返回 null")
    void encrypt_nullInput_returnsNull() {
        assertThat(sm4Service.encrypt(null)).isNull();
        assertThat(sm4Service.decrypt(null)).isNull();
    }

    @Test
    @DisplayName("解密 - 密钥错误时抛出异常")
    void decrypt_wrongKey_throwsException() {
        String plainText = "test data";
        String encrypted = sm4Service.encrypt(plainText);

        // 用错误的密钥尝试解密
        when(keyProvider.getKey()).thenReturn("abcdefghijklmnop".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> sm4Service.decrypt(encrypted))
            .isInstanceOf(Sm4EncryptionException.class)
            .hasMessageContaining("decryption failed");
    }

    @Test
    @DisplayName("解密 - 篡改密文时抛出异常")
    void decrypt_tamperedCipherText_throwsException() {
        String plainText = "original data";
        String encrypted = sm4Service.encrypt(plainText);
        byte[] decoded = Base64.decode(encrypted);
        decoded[16] ^= 0xFF; // 篡改密文
        String tampered = Base64.toBase64String(decoded);

        assertThatThrownBy(() -> sm4Service.decrypt(tampered))
            .isInstanceOf(Sm4EncryptionException.class);
    }
}
