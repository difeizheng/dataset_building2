package com.ctg.integration.security;

import com.ctg.integration.crypto.SMCryptoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * MFA挑战-响应服务
 * 基于SM2国密算法实现双因子认证
 * 使用Redis存储挑战，支持分布式部署
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
    private final StringRedisTemplate redisTemplate;

    // Redis key前缀
    private static final String REDIS_KEY_PREFIX = "mfa:challenge:";

    // 挑战有效期（秒）
    private static final long CHALLENGE_EXPIRY_SECONDS = 5 * 60; // 5分钟

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

        // 存储到Redis，设置过期时间
        String redisKey = REDIS_KEY_PREFIX + sessionId;
        redisTemplate.opsForValue().set(
            redisKey,
            challenge,
            CHALLENGE_EXPIRY_SECONDS,
            TimeUnit.SECONDS
        );

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
        String redisKey = REDIS_KEY_PREFIX + sessionId;
        String challenge = redisTemplate.opsForValue().get(redisKey);

        if (challenge == null) {
            log.warn("MFA挑战不存在或已过期: sessionId={}", sessionId);
            return false;
        }

        try {
            // 使用SM2公钥验证签名
            byte[] challengeBytes = challenge.getBytes(StandardCharsets.UTF_8);

            // 验证SM2签名
            boolean verified = smCryptoUtils.sm2Verify(challengeBytes, signatureBase64, publicKeyBase64);

            if (verified) {
                // 验证成功，移除挑战（一次性使用）
                redisTemplate.delete(redisKey);
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
        String redisKey = REDIS_KEY_PREFIX + sessionId;
        redisTemplate.delete(redisKey);
    }
}
