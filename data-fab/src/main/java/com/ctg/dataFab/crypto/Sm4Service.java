package com.ctg.dataFab.crypto;

/**
 * 国密 SM4 加解密服务接口
 *
 * <p>提供 L4 核心数据的透明加解密能力：
 * <ul>
 *   <li>SM4-CBC 模式（GB/T 32907-2016）</li>
 *   <li>密钥从环境变量注入，禁止硬编码</li>
 *   <li>异常统一封装为 {@link Sm4EncryptionException}</li>
 * </ul>
 *
 * @author Security Officer
 * @since 2026-07-05
 */
public interface Sm4Service {

    /**
     * SM4-CBC 加密
     *
     * @param plainText 明文（UTF-8）
     * @return Base64 编码的密文
     * @throws Sm4EncryptionException 加密失败时抛出
     */
    String encrypt(String plainText);

    /**
     * SM4-CBC 解密
     *
     * @param cipherText Base64 编码的密文
     * @return 解密后的明文（UTF-8）
     * @throws Sm4EncryptionException 解密失败时抛出（密钥错误、格式损坏等）
     */
    String decrypt(String cipherText);

    /**
     * 判断是否已初始化（密钥可用）
     *
     * @return true 表示密钥已注入且可用
     */
    boolean isInitialized();
}
