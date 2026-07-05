package com.ctg.dataFab.crypto;

/**
 * SM4 密钥提供者接口
 * 用于抽象密钥管理，支持环境变量、KMS 等多种密钥来源
 *
 * @author Security Officer
 * @since 2026-07-05
 */
public interface Sm4KeyProvider {

    /**
     * 获取 SM4 密钥（128位 = 16字节）
     *
     * @return 密钥字节数组
     * @throws Sm4EncryptionException 密钥未配置或格式错误时抛出
     */
    byte[] getKey();
}
