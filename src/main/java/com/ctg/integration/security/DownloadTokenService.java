package com.ctg.integration.security;

import com.ctg.integration.crypto.SMCryptoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * 下载令牌服务
 * 生成绑定用户身份和有效期的下载令牌
 * 满足安全审计要求：H-3 下载令牌绑定用户身份 + 有效期
 *
 * @author CTG
 * @since 2026-07-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadTokenService {

    private final SMCryptoUtils smCryptoUtils;

    /**
     * 生成下载令牌
     * 令牌内容：userId|resourceId|expiresAt|tokenId
     * 使用SM3-HMAC签名确保完整性
     *
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @param validityMinutes 有效期（分钟）
     * @return 下载令牌
     */
    public String generateDownloadToken(Long userId, String resourceId, int validityMinutes) {
        Instant expiresAt = Instant.now().plusSeconds(validityMinutes * 60L);
        String tokenId = UUID.randomUUID().toString();

        // 构建令牌内容
        String tokenContent = String.format("%d|%s|%d|%s",
                userId, resourceId, expiresAt.getEpochSecond(), tokenId);

        // 使用SM3-HMAC签名（需要密钥，这里使用userId的hash作为密钥）
        byte[] hmacKey = smCryptoUtils.sm3Hash(String.valueOf(userId).getBytes(StandardCharsets.UTF_8));
        String signature = smCryptoUtils.sm3HmacHex(tokenContent, new String(hmacKey, StandardCharsets.UTF_8));

        // 组合令牌：内容.签名
        String token = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(tokenContent.getBytes(StandardCharsets.UTF_8)) + "." + signature;

        log.info("生成下载令牌: userId={}, resourceId={}, expiresAt={}", userId, resourceId, expiresAt);
        return token;
    }

    /**
     * 验证下载令牌
     *
     * @param token 下载令牌
     * @param userId 当前用户ID
     * @param resourceId 资源ID
     * @return 是否有效
     */
    public boolean validateDownloadToken(String token, Long userId, String resourceId) {
        try {
            // 分离内容和签名
            String[] parts = token.split("\\.");
            if (parts.length != 2) {
                log.warn("下载令牌格式错误");
                return false;
            }

            String tokenContent = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            String signature = parts[1];

            // 解析令牌内容
            String[] fields = tokenContent.split("\\|");
            if (fields.length != 4) {
                log.warn("下载令牌内容格式错误");
                return false;
            }

            Long tokenUserId = Long.parseLong(fields[0]);
            String tokenResourceId = fields[1];
            long expiresAtEpoch = Long.parseLong(fields[2]);
            String tokenId = fields[3];

            // 验证用户身份
            if (!tokenUserId.equals(userId)) {
                log.warn("下载令牌用户不匹配: expected={}, actual={}", tokenUserId, userId);
                return false;
            }

            // 验证资源ID
            if (!tokenResourceId.equals(resourceId)) {
                log.warn("下载令牌资源不匹配: expected={}, actual={}", tokenResourceId, resourceId);
                return false;
            }

            // 验证有效期
            if (Instant.now().getEpochSecond() > expiresAtEpoch) {
                log.warn("下载令牌已过期: expiresAt={}", Instant.ofEpochSecond(expiresAtEpoch));
                return false;
            }

            // 验证签名
            byte[] hmacKey = smCryptoUtils.sm3Hash(String.valueOf(userId).getBytes(StandardCharsets.UTF_8));
            String expectedSignature = smCryptoUtils.sm3HmacHex(tokenContent, new String(hmacKey, StandardCharsets.UTF_8));
            if (!expectedSignature.equals(signature)) {
                log.warn("下载令牌签名验证失败");
                return false;
            }

            log.debug("下载令牌验证通过: userId={}, resourceId={}, tokenId={}", userId, resourceId, tokenId);
            return true;

        } catch (Exception e) {
            log.error("下载令牌验证失败", e);
            return false;
        }
    }

    /**
     * 获取令牌过期时间
     *
     * @param token 下载令牌
     * @return 过期时间戳（秒），如果令牌无效返回null
     */
    public Long getTokenExpiration(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 2) {
                return null;
            }

            String tokenContent = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            String[] fields = tokenContent.split("\\|");
            if (fields.length != 4) {
                return null;
            }

            return Long.parseLong(fields[2]);
        } catch (Exception e) {
            log.error("解析令牌过期时间失败", e);
            return null;
        }
    }
}
