package com.ctg.integration.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MFA挑战响应DTO
 *
 * @author CTG
 * @since 2026-07-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MfaChallengeResponse {
    /**
     * 挑战字符串（Base64编码）
     */
    private String challenge;

    /**
     * 会话ID
     */
    private String sessionId;
}
