package com.ctg.aiFab.gateway.service;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * MFA双因子认证服务
 *
 * @author Developer
 * @since 2026-07-02
 */
@Slf4j
@Service
public class MfaService {

    private final SecretGenerator secretGenerator;
    private final CodeVerifier codeVerifier;

    public MfaService() {
        this.secretGenerator = new DefaultSecretGenerator();
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        this.codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
    }

    /**
     * 生成新的TOTP密钥
     */
    public String generateSecret() {
        return secretGenerator.generate();
    }

    /**
     * 验证TOTP代码
     *
     * @param secret 用户的TOTP密钥
     * @param code 用户输入的6位数字代码
     * @return 验证是否成功
     */
    public boolean verifyCode(String secret, String code) {
        try {
            return codeVerifier.isValidCode(secret, code);
        } catch (Exception e) {
            log.error("TOTP验证失败: {}", e.getMessage());
            return false;
        }
    }
}
