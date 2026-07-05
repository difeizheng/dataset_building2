package com.ctg.dataFab.crypto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Security;
import java.util.Arrays;

/**
 * 国密 SM4-CBC 加解密实现
 * 使用 Bouncy Castle SM4Engine 实现，CBC 模式，PKCS7 填充
 *
 * @author Security Officer
 * @since 2026-07-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Sm4CbcServiceImpl implements Sm4Service {

    private static final String ALGORITHM = "SM4";
    private static final String TRANSFORMATION = "SM4/CBC/PKCS7Padding";
    private static final int IV_LENGTH = 16;

    private final Sm4KeyProvider keyProvider;

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @Override
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            return plainText;
        }

        try {
            byte[] key = keyProvider.getKey();
            byte[] iv = generateRandomIV();

            Cipher cipher = Cipher.getInstance(TRANSFORMATION, BouncyCastleProvider.PROVIDER_NAME);
            SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            byte[] plainBytes = plainText.getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = cipher.doFinal(plainBytes);

            // Prepend IV to ciphertext for storage
            byte[] result = new byte[IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, result, 0, IV_LENGTH);
            System.arraycopy(encrypted, 0, result, IV_LENGTH, encrypted.length);

            return Base64.toBase64String(result);
        } catch (Exception e) {
            if (e instanceof Sm4EncryptionException) {
                throw (Sm4EncryptionException) e;
            }
            throw new Sm4EncryptionException("SM4 encryption failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isBlank()) {
            return cipherText;
        }

        try {
            byte[] key = keyProvider.getKey();
            byte[] encryptedData = Base64.decode(cipherText);

            // Extract IV from the beginning
            byte[] iv = Arrays.copyOfRange(encryptedData, 0, IV_LENGTH);
            byte[] ciphertext = Arrays.copyOfRange(encryptedData, IV_LENGTH, encryptedData.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION, BouncyCastleProvider.PROVIDER_NAME);
            SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            byte[] decrypted = cipher.doFinal(ciphertext);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            if (e instanceof Sm4EncryptionException) {
                throw (Sm4EncryptionException) e;
            }
            throw new Sm4EncryptionException("SM4 decryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * 生成随机 IV（16字节）
     */
    private byte[] generateRandomIV() {
        byte[] iv = new byte[IV_LENGTH];
        new java.security.SecureRandom().nextBytes(iv);
        return iv;
    }
}
