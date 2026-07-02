package com.ctg.aiFab.gateway.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT密钥强度验证器
 * 确保生产环境不使用弱密钥
 */
@Slf4j
@Component
public class JwtSecretValidator {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @PostConstruct
    public void validate() {
        if (jwtSecret == null || jwtSecret.length() < 32) {
            throw new IllegalStateException(
                "JWT密钥长度必须至少32个字符，当前长度: " +
                (jwtSecret == null ? 0 : jwtSecret.length())
            );
        }

        if (jwtSecret.equals("your-secret-key-change-in-production")) {
            throw new IllegalStateException(
                "禁止使用默认JWT密钥，请在配置文件中设置强随机密钥"
            );
        }

        log.info("JWT密钥强度验证通过");
    }
}
