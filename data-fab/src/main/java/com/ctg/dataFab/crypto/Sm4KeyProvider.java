package com.ctg.dataFab.crypto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import cn.hutool.core.codec.Base64;

/**
 * SM4 密钥提供者
 *
 * <p>从 Spring 配置 {@code sm4.key} 注入密钥（Base64 编码的 16 字节）。
 * 启动时校验非空且长度必须为 16 字节（128 bit，符合 GB/T 32907-2016）。
 *
 * <p>部署时通过环境变量配置：
 * <pre>
 * SM4_KEY_BASE64=&lt;32 字符的 Base64 字符串&gt;
 * </pre>
 *
 * @author Security Officer
 * @since 2026-07-05
 */
@Slf4j
@Component
public class Sm4KeyProvider {

    /** Base64 编码的 SM4 密钥（16 字节明文 → 约 24 字符 Base64） */
    @Value("${sm4.key}")
    private String keyBase64;

    /**
     * 返回解密后的原始 16 字节密钥
     *
     * @return 128 bit SM4 密钥
     * @throws IllegalStateException 密钥未配置或长度错误
     */
    public byte[] getKeyBytes() {
        if (keyBase64 == null || keyBase64.isBlank()) {
            throw new IllegalStateException(
                "SM4 密钥未配置！请设置环境变量 sm4.key（Base64 编码的 16 字节）");
        }
        byte[] raw = Base64.decode(keyBase64);
        if (raw.length != 16) {
            throw new IllegalStateException(
                "SM4 密钥长度错误：期望 16 字节，实际 " + raw.length + " 字节");
        }
        log.info("SM4 密钥已加载（16 字节）");
        return raw;
    }

    /**
     * 返回原始 Base64 字符串（供日志脱敏使用）
     */
    public String getKeyBase64() {
        return keyBase64;
    }
}
