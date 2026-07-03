package com.ctg.integration.service;

import java.util.List;

import com.ctg.integration.dto.auth.*;

/**
 * 认证服务接口
 *
 * @author CTG
 * @since 2026-07-01
 */
public interface AuthenticationService {

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 刷新令牌
     */
    LoginResponse refreshToken(RefreshTokenRequest request);

    /**
     * 用户登出
     */
    void logout(String token);

    /**
     * 生成MFA挑战
     */
    MfaChallengeResponse generateMfaChallenge(String sessionId);

    /**
     * 验证MFA响应
     */
    boolean verifyMfaResponse(String sessionId, String signatureBase64, String publicKeyBase64);

    /**
     * 双因子认证验证（兼容旧接口）
     */
    boolean verifyMFA(String token, String code);

    /**
     * 修改密码
     */
    void changePassword(ChangePasswordRequest request);

    /**
     * 获取用户权限
     */
    List<String> getUserPermissions(String username);
}
