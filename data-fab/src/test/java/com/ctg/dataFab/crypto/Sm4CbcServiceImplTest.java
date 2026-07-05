package com.ctg.dataFab.crypto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import cn.hutool.core.codec.Base64;

/**
 * SM4 加解密服务单元测试
 *
 * <p>覆盖：
 * <ul>
 *   <li>加密/解密往返（往返一致性）</li>
 *   <li>空字符串输入</li>
 *   <li>空值输入</li>
 *   <li>错误密钥解密</li>
 *   <li>损坏的密文解密</li>
 *   <li>isInitialized 状态</li>
 * </ul>
 *
 * @author Security Officer
 * @since 2026-07-05
 */
@DisplayName("Sm4CbcServiceImpl 单元测试")
class Sm4CbcServiceImplTest {

    /** 16 字节全零测试密钥（Base64 编码） */
    private static final String TEST_KEY_BASE64 = Base64.encode(new byte[16]);

    private Sm4CbcServiceImpl sm4Service;

    @BeforeEach
    void setUp() {
        // 注入测试密钥
        Sm4KeyProvider keyProvider = new Sm4KeyProvider();
        ReflectionTestUtils.setField(keyProvider, "keyBase64", TEST_KEY_BASE64);
        sm4Service = new Sm4CbcServiceImpl(keyProvider);
    }

    @Test
    @DisplayName("encrypt + decrypt 往返一致性")
    void encryptDecrypt_roundTrip_returnsOriginalText() {
        String original = "三峡招标核心数据集 L4";
        String cipher = sm4Service.encrypt(original);
        String decrypted = sm4Service.decrypt(cipher);

        Assertions.assertEquals(original, decrypted);
    }

    @Test
    @DisplayName("encrypt 每次生成不同密文（随机 IV）")
    void encrypt_sameText_differentCipherTexts() {
        String original = "核心数据";
        String cipher1 = sm4Service.encrypt(original);
        String cipher2 = sm4Service.encrypt(original);

        // 随机 IV 导致密文不同
        Assertions.assertNotEquals(cipher1, cipher2);
        // 但都能正确解密
        Assertions.assertEquals(original, sm4Service.decrypt(cipher1));
        Assertions.assertEquals(original, sm4Service.decrypt(cipher2));
    }

    @Test
    @DisplayName("空字符串输入直接返回")
    void encrypt_emptyString_returnsEmpty() {
        Assertions.assertEquals("", sm4Service.encrypt(""));
        Assertions.assertEquals("", sm4Service.decrypt(""));
    }

    @Test
    @DisplayName("null 输入直接返回")
    void encrypt_null_returnsNull() {
        Assertions.assertNull(sm4Service.encrypt(null));
        Assertions.assertNull(sm4Service.decrypt(null));
    }

    @Test
    @DisplayName("错误密钥解密抛出 Sm4EncryptionException")
    void decrypt_wrongKey_throwsException() {
        String original = "秘密数据";
        String cipher = sm4Service.encrypt(original);

        // 用另一个有效 16 字节密钥的 provider 解密会失败
        Sm4KeyProvider wrongProvider = new Sm4KeyProvider();
        ReflectionTestUtils.setField(wrongProvider, "keyBase64",
            Base64.encode("abcdefghijklmnop".getBytes())); // 另一个 16 字节密钥
        Sm4CbcServiceImpl wrongService = new Sm4CbcServiceImpl(wrongProvider);

        Assertions.assertThrows(Sm4EncryptionException.class, () -> {
            wrongService.decrypt(cipher);
        });
    }

    @Test
    @DisplayName("损坏的密文解密抛出 Sm4EncryptionException")
    void decrypt_corruptCipherText_throwsException() {
        Assertions.assertThrows(Sm4EncryptionException.class, () -> {
            sm4Service.decrypt("这不是有效的Base64密文!!!");
        });
    }

    @Test
    @DisplayName("isInitialized 返回 true（密钥有效）")
    void isInitialized_withValidKey_returnsTrue() {
        Assertions.assertTrue(sm4Service.isInitialized());
    }

    @Test
    @DisplayName("Sm4KeyProvider 缺失密钥时抛出 IllegalStateException")
    void getKeyBytes_missingKey_throws() {
        Sm4KeyProvider emptyProvider = new Sm4KeyProvider();
        ReflectionTestUtils.setField(emptyProvider, "keyBase64", "");

        Assertions.assertThrows(IllegalStateException.class, emptyProvider::getKeyBytes);
    }

    @Test
    @DisplayName("Sm4KeyProvider 错误长度密钥时抛出 IllegalStateException")
    void getKeyBytes_wrongLength_throws() {
        Sm4KeyProvider wrongProvider = new Sm4KeyProvider();
        ReflectionTestUtils.setField(wrongProvider, "keyBase64",
            Base64.encode("short".getBytes()));

        Assertions.assertThrows(IllegalStateException.class, wrongProvider::getKeyBytes);
    }
}
