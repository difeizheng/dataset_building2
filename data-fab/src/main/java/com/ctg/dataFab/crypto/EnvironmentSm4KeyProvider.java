package com.ctg.dataFab.crypto;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;

/**
 * 基于环境变量的 SM4 密钥提供者
 * 密钥必须通过环境变量 SM4_ENCRYPTION_KEY 注入，禁止硬编码
 *
 * @author Security Officer
 * @since 2026-07-05
 */
@Slf4j
@Component
public class EnvironmentSm4KeyProvider implements Sm4KeyProvider {

    private static final int SM4_KEY_LENGTH = 16;

    @Value("${sm4.encryption.key:}")
    private String encryptionKey;

    /**
     * 启动时 fail-fast 校验：密钥必须已配置且格式正确，否则启动失败
     */
    @PostConstruct
    public void validateKeyConfiguration() {
        if (encryptionKey == null || encryptionKey.isBlank()) {
            throw new Sm4EncryptionException(
                "SM4 encryption key is not configured. " +
                "Please set the SM4_ENCRYPTION_KEY environment variable with a Base64-encoded 16-byte key."
            );
        }

        try {
            byte[] keyBytes = Base64.getDecoder().decode(encryptionKey);
            if (keyBytes.length != SM4_KEY_LENGTH) {
                throw new Sm4EncryptionException(
                    "SM4 key must be exactly 16 bytes (128 bits), but got " + keyBytes.length + " bytes"
                );
            }
        } catch (IllegalArgumentException e) {
            throw new Sm4EncryptionException("SM4 encryption key is not valid Base64 format", e);
        }

        log.info("SM4 encryption key configured successfully (16 bytes)");
    }

    @Override
    public byte[] getKey() {
        // 启动时已校验，此处直接解码（已缓存逻辑由 Spring 容器管理）
        return Base64.getDecoder().decode(encryptionKey);
    }
}
