package com.ctg.dataFab.crypto;

import cn.hutool.core.codec.Base64;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.engines.SM4Engine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

/**
 * SM4-CBC 模式实现（原生 BouncyCastle + PKCS7Padding）
 *
 * <p>GB/T 32907-2016，128-bit 分组密码，CBC 模式，PKCS7Padding。
 * 每个加密操作使用随机 IV（16 字节），嵌入密文头部，解密时提取。
 *
 * <p>密文格式：{@code IV (16 bytes) || SM4-CBC-PKCS7(plainText)}
 *
 * @author Security Officer
 * @since 2026-07-05
 */
@Slf4j
@Service
public class Sm4CbcServiceImpl implements Sm4Service {

    /** SM4 密钥（16 字节 / 128 bit） */
    private final byte[] key;

    /** 密文随机源 */
    private final SecureRandom secureRandom;

    public Sm4CbcServiceImpl(Sm4KeyProvider keyProvider) {
        this.key = keyProvider.getKeyBytes();
        this.secureRandom = new SecureRandom();
    }

    @Override
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        try {
            byte[] iv = generateRandomIv();
            byte[] plainBytes = plainText.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            byte[] cipherBytes = sm4Cbc(iv, plainBytes, true);

            // 拼接 IV || 密文
            byte[] ivAndCipher = new byte[iv.length + cipherBytes.length];
            System.arraycopy(iv, 0, ivAndCipher, 0, iv.length);
            System.arraycopy(cipherBytes, 0, ivAndCipher, iv.length, cipherBytes.length);

            return Base64.encode(ivAndCipher);
        } catch (Exception e) {
            throw new Sm4EncryptionException("SM4 加密失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }
        try {
            byte[] ivAndCipher = Base64.decode(cipherText);
            if (ivAndCipher.length < 17) {
                throw new Sm4EncryptionException("密文长度不足（至少需要 17 字节）");
            }

            // 提取前 16 字节 IV
            byte[] iv = new byte[16];
            byte[] cipherBytes = new byte[ivAndCipher.length - 16];
            System.arraycopy(ivAndCipher, 0, iv, 0, 16);
            System.arraycopy(ivAndCipher, 16, cipherBytes, 0, cipherBytes.length);

            byte[] plainBytes = sm4Cbc(iv, cipherBytes, false);
            return new String(plainBytes, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Sm4EncryptionException e) {
            throw e;
        } catch (Exception e) {
            throw new Sm4EncryptionException("SM4 解密失败，可能是密钥错误或密文已损坏: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isInitialized() {
        return key != null && key.length == 16;
    }

    /** 生成 16 字节随机 IV */
    private byte[] generateRandomIv() {
        byte[] iv = new byte[16];
        secureRandom.nextBytes(iv);
        return iv;
    }

    /**
     * SM4-CBC 加解密
     *
     * @param iv             16 字节 IV
     * @param input          明文或密文
     * @param forEncryption  true=加密，false=解密
     * @return 加解密结果
     */
    private byte[] sm4Cbc(byte[] iv, byte[] input, boolean forEncryption) throws Exception {
        // PaddedBufferedBlockCipher 自动处理 PKCS7Padding
        BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(
                new CBCBlockCipher(new SM4Engine()));

        KeyParameter keyParam = new KeyParameter(key);
        ParametersWithIV params = new ParametersWithIV(keyParam, iv);
        cipher.init(forEncryption, params);

        int outputLen = cipher.getOutputSize(input.length);
        byte[] output = new byte[outputLen];

        int len = cipher.processBytes(input, 0, input.length, output, 0);
        len += cipher.doFinal(output, len);

        // 截取实际长度
        byte[] result = new byte[len];
        System.arraycopy(output, 0, result, 0, len);
        return result;
    }
}
