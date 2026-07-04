package com.ctg.integration.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ctg.integration.dto.auth.*;
import com.ctg.integration.entity.AuditLog;
import com.ctg.integration.entity.User;
import com.ctg.integration.entity.UserSession;
import com.ctg.integration.exception.AccountLockedException;
import com.ctg.integration.exception.MFARequiredException;
import com.ctg.integration.repository.AuditLogRepository;
import com.ctg.integration.repository.UserRepository;
import com.ctg.integration.repository.UserSessionRepository;
import com.ctg.integration.security.JwtTokenProvider;
import com.ctg.integration.security.MfaChallengeService;
import com.ctg.integration.service.AuthenticationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 认证服务实现
 * 支持SSO单点登录、双因子认证、会话管理
 *
 * @author CTG
 * @since 2026-07-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final int MAX_LOGIN_FAIL_COUNT = 5;
    private static final int LOCK_DURATION_MINUTES = 30;

    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final AuditLogRepository auditLogRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final MfaChallengeService mfaChallengeService;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("用户登录尝试: {}", request.getUsername());

        // 1. 查找用户
        User user = userRepository.findByUsernameWithRoles(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + request.getUsername()));

        // 2. 检查账户是否被锁定
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            recordAuditLog(user, AuditLog.OperationType.LOGIN_FAIL, "账户已锁定", request.getClientIp());
            throw new AccountLockedException("账户已锁定，请在 " + LOCK_DURATION_MINUTES + " 分钟后重试");
        }

        // 3. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            handleLoginFailure(user, request);
            throw new BadCredentialsException("密码错误");
        }

        // 4. 检查账户是否启用
        if (!user.getEnabled()) {
            recordAuditLog(user, AuditLog.OperationType.LOGIN_FAIL, "账户已禁用", request.getClientIp());
            throw new UsernameNotFoundException("账户已禁用");
        }

        // 5. 检查是否需要双因子认证
        // 在生产环境中，这里应该检查用户是否启用了MFA
        // 暂时跳过MFA检查，直接生成令牌

        // 6. 生成JWT令牌
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getCode())
                .collect(Collectors.toList());

        String accessToken = jwtTokenProvider.generateToken(user.getUsername(), roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

        // 7. 创建会话记录
        UserSession session = UserSession.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .sessionToken(accessToken)
                .refreshToken(refreshToken)
                .clientIp(request.getClientIp())
                .userAgent(request.getUserAgent())
                .loginAt(LocalDateTime.now())
                .expireAt(LocalDateTime.now().plusSeconds(3600))
                .lastActiveAt(LocalDateTime.now())
                .isActive(true)
                .mfaVerified(false)
                .build();
        sessionRepository.save(session);

        // 8. 更新用户登录信息
        user.setLastLoginAt(LocalDateTime.now());
        user.setLoginFailCount(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        // 9. 记录审计日志
        recordAuditLog(user, AuditLog.OperationType.LOGIN, "登录成功", request.getClientIp());

        // 10. 获取用户权限
        List<String> permissions = getUserPermissions(user.getUsername());

        log.info("用户登录成功: {}", user.getUsername());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .username(user.getUsername())
                .realName(user.getRealName())
                .roles(roles)
                .permissions(permissions)
                .mfaRequired(false)
                .build();
    }

    @Override
    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        log.debug("刷新令牌请求");

        // 1. 验证刷新令牌
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new BadCredentialsException("刷新令牌无效或已过期");
        }

        // 2. 查找会话
        UserSession session = sessionRepository.findByRefreshToken(request.getRefreshToken())
                .orElseThrow(() -> new BadCredentialsException("会话不存在"));

        if (!session.getIsActive() || session.isExpired()) {
            throw new BadCredentialsException("会话已失效");
        }

        // 3. 查找用户
        User user = userRepository.findByUsernameWithRoles(session.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

        // 4. 生成新令牌
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getCode())
                .collect(Collectors.toList());

        String newAccessToken = jwtTokenProvider.generateToken(user.getUsername(), roles);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

        // 5. 更新会话
        session.setSessionToken(newAccessToken);
        session.setRefreshToken(newRefreshToken);
        session.setLastActiveAt(LocalDateTime.now());
        session.setExpireAt(LocalDateTime.now().plusSeconds(3600));
        sessionRepository.save(session);

        List<String> permissions = getUserPermissions(user.getUsername());

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .username(user.getUsername())
                .realName(user.getRealName())
                .roles(roles)
                .permissions(permissions)
                .mfaRequired(false)
                .build();
    }

    @Override
    @Transactional
    public void logout(String token) {
        log.debug("用户登出");

        try {
            String username = jwtTokenProvider.getUsernameFromToken(token);
            User user = userRepository.findByUsername(username)
                    .orElse(null);

            // 失效所有会话
            if (user != null) {
                sessionRepository.invalidateAllUserSessions(user.getId());
                recordAuditLog(user, AuditLog.OperationType.LOGOUT, "用户登出", null);
            }
        } catch (Exception e) {
            log.warn("登出时令牌验证失败", e);
        }
    }

    @Override
    public MfaChallengeResponse generateMfaChallenge(String sessionId) {
        log.debug("生成MFA挑战: sessionId={}", sessionId);
        String challenge = mfaChallengeService.generateChallenge(sessionId);
        return MfaChallengeResponse.builder()
                .challenge(challenge)
                .sessionId(sessionId)
                .build();
    }

    @Override
    @Transactional
    public boolean verifyMfaResponse(String sessionId, String signatureBase64, String publicKeyBase64) {
        log.debug("验证MFA响应: sessionId={}", sessionId);

        boolean verified = mfaChallengeService.verifyResponse(sessionId, signatureBase64, publicKeyBase64);

        if (verified) {
            // 查找会话并标记MFA已验证
            UserSession session = sessionRepository.findBySessionToken(sessionId)
                    .orElse(null);
            if (session != null) {
                session.setMfaVerified(true);
                sessionRepository.save(session);
                log.info("MFA验证成功，会话已标记: sessionId={}", sessionId);
            }
        }

        return verified;
    }

    @Override
    @Transactional
    public boolean verifyMFA(String token, String code) {
        log.warn("verifyMFA(token, code)已废弃，请使用generateMfaChallenge和verifyMfaResponse");
        return false;
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        log.info("修改密码请求");

        // 1. 验证两次密码输入一致
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("两次输入的密码不一致");
        }

        // 2. 获取当前用户（从SecurityContext）
        String username = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        // 3. 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
            throw new BadCredentialsException("旧密码错误");
        }

        // 4. 更新密码
        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);

        // 5. 失效所有会话
        sessionRepository.invalidateAllUserSessions(currentUser.getId());

        // 6. 记录审计日志
        recordAuditLog(currentUser, AuditLog.OperationType.PASSWORD_CHANGE, "修改密码", null);

        log.info("密码修改成功: username={}", username);
    }

    @Override
    public List<String> getUserPermissions(String username) {
        User user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getCode())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 处理登录失败
     */
    private void handleLoginFailure(User user, LoginRequest request) {
        int failCount = user.getLoginFailCount() + 1;
        user.setLoginFailCount(failCount);

        if (failCount >= MAX_LOGIN_FAIL_COUNT) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
            log.warn("用户 {} 登录失败次数过多，账户已锁定", user.getUsername());
        }

        userRepository.save(user);
        recordAuditLog(user, AuditLog.OperationType.LOGIN_FAIL, "登录失败，失败次数: " + failCount, request.getClientIp());
    }

    /**
     * 记录审计日志
     */
    private void recordAuditLog(User user, AuditLog.OperationType operation, String description, String clientIp) {
        AuditLog auditLog = AuditLog.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .clientIp(clientIp)
                .operation(operation)
                .description(description)
                .result(operation == AuditLog.OperationType.LOGIN_FAIL ? AuditLog.ResultType.FAILURE : AuditLog.ResultType.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();
        auditLogRepository.save(auditLog);
    }
}
