package com.ctg.integration.crypto;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.ECPoint;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * 国密SM2/SM3/SM4算法工具类
 * 基于Bouncy Castle实现，满足国产化要求
 *
 * SM2: 非对称加密（256位椭圆曲线）
 * SM3: 哈希摘要（256位）
 * SM4: 对称加密（128位分组，CBC模式）
 *
 * @author CTG
 * @since 2026-07-01
 */
@Slf4j
@Component
public class SMCryptoUtils {

    private static final X9ECParameters SM2_PARAMS = GMNamedCurves.getByName("sm2p256v1");
    private static final ECDomainParameters SM2_DOMAIN = new ECDomainParameters(
            SM2_PARAMS.getCurve(), SM2_PARAMS.getG(), SM2_PARAMS.getN(), SM2_PARAMS.getH());

    @PostConstruct
    public void init() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    // ==================== SM2 非对称加密 ====================

    /**
     * 生成SM2密钥对
     * @return SM2KeyPair 包含公钥和私钥（Base64编码）
     */
    public SM2KeyPair generateSM2KeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");
            keyPairGenerator.initialize(new ECGenParameterSpec("sm2p256v1"), new SecureRandom());
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
            String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

            return new SM2KeyPair(publicKey, privateKey);
        } catch (Exception e) {
            log.error("SM2密钥对生成失败", e);
            throw new CryptoException("SM2密钥对生成失败", e);
        }
    }

    /**
     * SM2公钥加密
     * @param data 待加密数据
     * @param publicKeyBase64 Base64编码的公钥
     * @return 加密后的数据（Base64编码）
     */
    public String sm2Encrypt(String data, String publicKeyBase64) {
        try {
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
            ECPoint publicKeyPoint = SM2_DOMAIN.getCurve().decodePoint(publicKeyBytes);
            ECPublicKeyParameters pubKeyParams = new ECPublicKeyParameters(publicKeyPoint, SM2_DOMAIN);

            SM2Engine engine = new SM2Engine(SM2Engine.Mode.C1C3C2);
            ParametersWithRandom params = new ParametersWithRandom(pubKeyParams, new SecureRandom());
            engine.init(true, params);

            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = engine.processBlock(dataBytes, 0, dataBytes.length);

            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("SM2加密失败", e);
            throw new CryptoException("SM2加密失败", e);
        }
    }

    /**
     * SM2私钥解密
     * @param encryptedBase64 Base64编码的加密数据
     * @param privateKeyBase64 Base64编码的私钥
     * @return 解密后的原始数据
     */
    public String sm2Decrypt(String encryptedBase64, String privateKeyBase64) {
        try {
            byte[] encrypted = Base64.getDecoder().decode(encryptedBase64);
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);

            // 从PKCS8格式提取私钥值
            BigInteger privateKeyValue = new BigInteger(1, Arrays.copyOfRange(privateKeyBytes, privateKeyBytes.length - 32, privateKeyBytes.length));
            ECPrivateKeyParameters privKeyParams = new ECPrivateKeyParameters(privateKeyValue, SM2_DOMAIN);

            SM2Engine engine = new SM2Engine(SM2Engine.Mode.C1C3C2);
            engine.init(false, privKeyParams);

            byte[] decrypted = engine.processBlock(encrypted, 0, encrypted.length);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("SM2解密失败", e);
            throw new CryptoException("SM2解密失败", e);
        }
    }

    // ==================== SM3 哈希摘要 ====================

    /**
     * SM3哈希计算
     * @param data 待哈希数据
     * @return 32字节哈希值
     */
    public byte[] sm3Hash(byte[] data) {
        try {
            org.bouncycastle.crypto.digests.SM3Digest digest = new org.bouncycastle.crypto.digests.SM3Digest();
            digest.update(data, 0, data.length);
            byte[] result = new byte[digest.getDigestSize()];
            digest.doFinal(result, 0);
            return result;
        } catch (Exception e) {
            log.error("SM3哈希计算失败", e);
            throw new CryptoException("SM3哈希计算失败", e);
        }
    }

    /**
     * SM3哈希计算（字符串输入）
     * @param data 待哈希字符串
     * @return 64字符十六进制哈希值
     */
    public String sm3HashHex(String data) {
        byte[] hash = sm3Hash(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    /**
     * SM3 HMAC计算
     * @param data 待计算数据
     * @param key 密钥
     * @return 32字节HMAC值
     */
    public byte[] sm3Hmac(byte[] data, byte[] key) {
        try {
            org.bouncycastle.crypto.macs.HMac hmac = new org.bouncycastle.crypto.macs.HMac(
                    new org.bouncycastle.crypto.digests.SM3Digest());
            hmac.init(new org.bouncycastle.crypto.params.KeyParameter(key));
            hmac.update(data, 0, data.length);
            byte[] result = new byte[hmac.getMacSize()];
            hmac.doFinal(result, 0);
            return result;
        } catch (Exception e) {
            log.error("SM3 HMAC计算失败", e);
            throw new CryptoException("SM3 HMAC计算失败", e);
        }
    }

    /**
     * SM3 HMAC计算（字符串版本）
     */
    public String sm3HmacHex(String data, String key) {
        byte[] hmac = sm3Hmac(data.getBytes(StandardCharsets.UTF_8), key.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hmac);
    }

    // ==================== SM4 对称加密 ====================

    /**
     * 生成SM4密钥（128位）
     * @return 16字节密钥
     */
    public byte[] generateSM4Key() {
        byte[] key = new byte[16];
        new SecureRandom().nextBytes(key);
        return key;
    }

    /**
     * SM4加密（CBC模式）
     * @param data 待加密数据
     * @param key 16字节密钥
     * @return IV + 密文（Base64编码）
     */
    public String sm4Encrypt(String data, byte[] key) {
        try {
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance("SM4/CBC/PKCS5Padding", "BC");
            SecretKeySpec keySpec = new SecretKeySpec(key, "SM4");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // IV + 密文
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(iv);
            out.write(encrypted);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            log.error("SM4加密失败", e);
            throw new CryptoException("SM4加密失败", e);
        }
    }

    /**
     * SM4解密（CBC模式）
     * @param encryptedBase64 Base64编码的IV+密文
     * @param key 16字节密钥
     * @return 解密后的原始数据
     */
    public String sm4Decrypt(String encryptedBase64, byte[] key) {
        try {
            byte[] encrypted = Base64.getDecoder().decode(encryptedBase64);
            byte[] iv = new byte[16];
            byte[] cipherText = new byte[encrypted.length - 16];
            System.arraycopy(encrypted, 0, iv, 0, 16);
            System.arraycopy(encrypted, 16, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance("SM4/CBC/PKCS5Padding", "BC");
            SecretKeySpec keySpec = new SecretKeySpec(key, "SM4");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decrypted = cipher.doFinal(cipherText);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("SM4解密失败", e);
            throw new CryptoException("SM4解密失败", e);
        }
    }

    /**
     * SM4加密（字节数组版本）
     */
    public byte[] sm4EncryptBytes(byte[] data, byte[] key) {
        try {
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance("SM4/CBC/PKCS5Padding", "BC");
            SecretKeySpec keySpec = new SecretKeySpec(key, "SM4");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(data);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(iv);
            out.write(encrypted);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("SM4加密失败", e);
            throw new CryptoException("SM4加密失败", e);
        }
    }

    /**
     * SM4解密（字节数组版本）
     */
    public byte[] sm4DecryptBytes(byte[] encryptedData, byte[] key) {
        try {
            byte[] iv = new byte[16];
            byte[] cipherText = new byte[encryptedData.length - 16];
            System.arraycopy(encryptedData, 0, iv, 0, 16);
            System.arraycopy(encryptedData, 16, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance("SM4/CBC/PKCS5Padding", "BC");
            SecretKeySpec keySpec = new SecretKeySpec(key, "SM4");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            return cipher.doFinal(cipherText);
        } catch (Exception e) {
            log.error("SM4解密失败", e);
            throw new CryptoException("SM4解密失败", e);
        }
    }

    // ==================== 辅助方法 ====================

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * SM2密钥对
     */
    public record SM2KeyPair(String publicKey, String privateKey) {}

    /**
     * 加密异常
     */
    public static class CryptoException extends RuntimeException {
        public CryptoException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
