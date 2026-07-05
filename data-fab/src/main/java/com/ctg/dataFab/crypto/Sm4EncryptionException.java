package com.ctg.dataFab.crypto;

/**
 * SM4 加密异常
 *
 * <p>封装 SM4 加解密过程中的所有错误：
 * <ul>
 *   <li>密钥未配置或格式错误</li>
 *   <li>密文格式损坏 / 非 Base64</li>
 *   <li>密钥不匹配解密失败</li>
 *   <li>IV 不匹配（数据被篡改）</li>
 * </ul>
 *
 * @author Security Officer
 * @since 2026-07-05
 */
public class Sm4EncryptionException extends RuntimeException {

    public Sm4EncryptionException(String message) {
        super(message);
    }

    public Sm4EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
