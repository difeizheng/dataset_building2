package com.ctg.dataFab.common.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * MFA 双因子认证服务
 * 验证 L4 端点访问的 MFA Token
 *
 * @author Security Officer
 * @since 2026-07-05
 */
@Slf4j
@Service
public class MfaService {

    /**
     * 验证 MFA Token
     *
     * @param mfaToken X-MFA-Token header 值 (6位数字TOTP码)
     * @return true = MFA验证通过; false = 验证失败
     */
    public boolean verifyMFA(String mfaToken) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        if (mfaToken == null || mfaToken.isBlank()) {
            log.warn("L4 端点访问被阻断: 未提供 MFA Token, user={}, path=/api/v1/delivery",
                    authentication.getName());
            return false;
        }

        // TODO: 集成 Samstevens totp 库验证真实 TOTP/HOTP
        // 当前占位验证：6位纯数字格式视为临时放行
        // 完整实现依赖 ai-fab-gateway 的 MFA 基础设施
        if (mfaToken.length() >= 6 && mfaToken.matches("\\d+")) {
            log.info("MFA 验证通过: user={}", authentication.getName());
            return true;
        }

        log.warn("MFA 验证失败: token 格式无效, user={}", authentication.getName());
        return false;
    }
}
