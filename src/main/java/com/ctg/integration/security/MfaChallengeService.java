package com.ctg.integration.security;

import com.ctg.integration.crypto.SMCryptoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MFA挑战-响应服务
 * 基于SM2国密算法实现双因子认证
 *
 * 流程：
 * 1. 用户请求MFA挑战 → 服务端生成随机挑战字符串
 * 2. 用户使用SM2私钥签名挑战 → 返回签名
 * 3. 服务端使用用户SM2公钥验证签名 → 通过则MFA验证成功
 *
 * @author CTG
 * @since 2026-07-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MfaChallengeService {

    private final SMCryptoUtils smCryptoUtils;

    // 存储活跃挑战：sessionId -> challenge
    private final Map<String, MfaChallenge> activeChallenges = new ConcurrentHashMap<>();

    // 挑战有效期（毫秒）
    private static final long CHALLENGE_EXPIRY_MS = 5 * 60 * 1000; // 5分钟

    /**
     * 生成MFA挑战
     * @param sessionId 会话ID
     * @return 挑战字符串（Base64编码）
     */
    public String generateChallenge(String sessionId) {
        // 生成随机挑战
        byte[] challengeBytes = new byte[32];
        new SecureRandom().nextBytes(challengeBytes);
        String challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(challengeBytes);

        // 存储挑战
        activeChallenges.put(sessionId, new MfaChallenge(
            challenge,
            System.currentTimeMillis() + CHALLENGE_EXPIRY_MS
        ));

        log.info("生成MFA挑战: sessionId={}", sessionId);
        return challenge;
    }

    /**
     * 验证MFA响应
     * @param sessionId 会话ID
     * @param signatureBase64 用户使用SM2私钥签名后的Base64编码
     * @param publicKeyBase64 用户SM2公钥（Base64编码）
     * @return 验证是否通过
     */
    public boolean verifyResponse(String sessionId, String signatureBase64, String publicKeyBase64) {
        MfaChallenge challenge = activeChallenges.get(sessionId);
        if (challenge == null) {
            log.warn("MFA挑战不存在或已过期: sessionId={}", sessionId);
            return false;
        }

        // 检查挑战是否过期
        if (System.currentTimeMillis() > challenge.expiryTime) {
            activeChallenges.remove(sessionId);
            log.warn("MFA挑战已过期: sessionId={}", sessionId);
            return false;
        }

        try {
            // 使用SM2公钥验证签名
            byte[] challengeBytes = challenge.challenge.getBytes(StandardCharsets.UTF_8);

            // 验证SM2签名
            boolean verified = smCryptoUtils.sm2Verify(challengeBytes, signatureBase64, publicKeyBase64);

            if (verified) {
                // 验证成功，移除挑战（一次性使用）
                activeChallenges.remove(sessionId);
                log.info("MFA验证成功: sessionId={}", sessionId);
            } else {
                log.warn("MFA签名验证失败: sessionId={}", sessionId);
            }

            return verified;
        } catch (Exception e) {
            log.error("MFA验证异常: sessionId={}", sessionId, e);
            return false;
        }
    }

    /**
     * 移除挑战（用于登出或超时清理）
     */
    public void removeChallenge(String sessionId) {
        activeChallenges.remove(sessionId);
    }

    /**
     * 清理过期挑战
     */
    public void cleanupExpiredChallenges() {
        long now = System.currentTimeMillis();
        activeChallenges.entrySet().removeIf(entry -> now > entry.getValue().expiryTime);
    }

    /**
     * MFA挑战记录
     */
    private record MfaChallenge(String challenge, long expiryTime) {}
}
