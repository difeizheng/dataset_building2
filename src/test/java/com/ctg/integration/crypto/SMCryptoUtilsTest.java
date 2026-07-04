package com.ctg.integration.crypto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 国密算法工具类测试
 *
 * @author CTG
 * @since 2026-07-01
 */
class SMCryptoUtilsTest {

    private SMCryptoUtils cryptoUtils;

    @BeforeEach
    void setUp() {
        cryptoUtils = new SMCryptoUtils();
        cryptoUtils.init();
    }

    @Test
    void testGenerateSM2KeyPair() {
        SMCryptoUtils.SM2KeyPair keyPair = cryptoUtils.generateSM2KeyPair();
        assertNotNull(keyPair);
        assertNotNull(keyPair.publicKey());
        assertNotNull(keyPair.privateKey());
        assertTrue(keyPair.publicKey().length() > 0);
        assertTrue(keyPair.privateKey().length() > 0);
    }

    @Test
    void testSM3Hash() {
        String data = "Hello, SM3!";
        String hash = cryptoUtils.sm3HashHex(data);
        assertNotNull(hash);
        assertEquals(64, hash.length()); // SM3输出32字节，即64个十六进制字符

        // 相同输入应该产生相同输出
        String hash2 = cryptoUtils.sm3HashHex(data);
        assertEquals(hash, hash2);

        // 不同输入应该产生不同输出
        String hash3 = cryptoUtils.sm3HashHex("Hello, SM4!");
        assertNotEquals(hash, hash3);
    }

    @Test
    void testSM3Hmac() {
        byte[] data = "test data".getBytes();
        byte[] key = "secret key".getBytes();
        byte[] hmac = cryptoUtils.sm3Hmac(data, key);
        assertNotNull(hmac);
        assertEquals(32, hmac.length); // SM3 HMAC输出32字节
    }

    @Test
    void testGenerateSM4Key() {
        byte[] key = cryptoUtils.generateSM4Key();
        assertNotNull(key);
        assertEquals(16, key.length); // SM4密钥128位，即16字节
    }

    @Test
    void testSM4EncryptDecrypt() {
        byte[] key = cryptoUtils.generateSM4Key();
        String plaintext = "Hello, SM4 encryption!";

        // 加密
        String encrypted = cryptoUtils.sm4Encrypt(plaintext, key);
        assertNotNull(encrypted);
        assertTrue(encrypted.length() > 0);

        // 解密
        String decrypted = cryptoUtils.sm4Decrypt(encrypted, key);
        assertNotNull(decrypted);
        assertEquals(plaintext, decrypted);
    }

    @Test
    void testSM4EncryptDecryptWithDifferentKeys() {
        byte[] key1 = cryptoUtils.generateSM4Key();
        byte[] key2 = cryptoUtils.generateSM4Key();
        String plaintext = "test data";

        String encrypted = cryptoUtils.sm4Encrypt(plaintext, key1);

        // 使用错误的密钥解密应该失败
        assertThrows(Exception.class, () -> {
            cryptoUtils.sm4Decrypt(encrypted, key2);
        });
    }

    @Test
    void testSM3HmacHex() {
        String data = "test data";
        String key = "secret key";
        String hmac = cryptoUtils.sm3HmacHex(data, key);
        assertNotNull(hmac);
        assertEquals(64, hmac.length()); // SM3 HMAC输出32字节，即64个十六进制字符
    }

    @Test
    void testSM4EncryptDecryptBytes() {
        byte[] key = cryptoUtils.generateSM4Key();
        byte[] data = "Hello, SM4 bytes!".getBytes();

        // 加密
        byte[] encrypted = cryptoUtils.sm4EncryptBytes(data, key);
        assertNotNull(encrypted);
        assertTrue(encrypted.length > data.length);

        // 解密
        byte[] decrypted = cryptoUtils.sm4DecryptBytes(encrypted, key);
        assertNotNull(decrypted);
        assertArrayEquals(data, decrypted);
    }
}
