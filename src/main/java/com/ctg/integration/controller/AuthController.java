package com.ctg.integration.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.ctg.integration.dto.auth.*;
import com.ctg.integration.security.JwtTokenProvider;
import com.ctg.integration.service.AuthenticationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 认证控制器
 * 提供登录、登出、令牌刷新等接口
 * 公开接口：login, refresh, validate
 * 需认证接口：logout, mfa/verify, password/change, permissions
 *
 * @author CTG
 * @since 2026-07-01
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户登录、登出、令牌管理")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名密码登录系统，返回JWT令牌（公开）")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("收到登录请求: {}", request.getUsername());
        LoginResponse response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌（公开）")
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("收到刷新令牌请求");
        LoginResponse response = authenticationService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "用户登出", description = "使当前令牌失效（需认证）")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization) {
        log.debug("收到登出请求");
        String token = authorization.substring(7); // Remove "Bearer "
        authenticationService.logout(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/mfa/challenge")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "生成MFA挑战", description = "生成SM2挑战-响应MFA挑战（需认证）")
    public ResponseEntity<MfaChallengeResponse> generateMfaChallenge(
            @RequestParam String sessionId) {
        log.debug("生成MFA挑战: sessionId={}", sessionId);
        MfaChallengeResponse response = authenticationService.generateMfaChallenge(sessionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/mfa/verify")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "验证MFA响应", description = "验证SM2签名响应（需认证）")
    public ResponseEntity<Boolean> verifyMfaResponse(
            @RequestParam String sessionId,
            @RequestParam String signature,
            @RequestParam String publicKey) {
        log.debug("验证MFA响应: sessionId={}", sessionId);
        boolean result = authenticationService.verifyMfaResponse(sessionId, signature, publicKey);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/password/change")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "修改密码", description = "修改当前用户密码（需认证）")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.debug("收到修改密码请求");
        authenticationService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/permissions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取权限", description = "获取当前用户权限列表（需认证）")
    public ResponseEntity<List<String>> getPermissions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        List<String> permissions = authenticationService.getUserPermissions(username);
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/validate")
    @Operation(summary = "验证令牌", description = "验证JWT令牌是否有效（公开）")
    public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String authorization) {
        String token = authorization.substring(7);
        boolean isValid = jwtTokenProvider.validateToken(token);
        return ResponseEntity.ok(isValid);
    }
}
