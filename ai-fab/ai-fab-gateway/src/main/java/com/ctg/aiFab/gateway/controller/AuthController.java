package com.ctg.aiFab.gateway.controller;

import com.ctg.aiFab.gateway.common.dto.ApiResponse;
import com.ctg.aiFab.gateway.dto.LoginRequest;
import com.ctg.aiFab.gateway.dto.LoginResponse;
import com.ctg.aiFab.gateway.dto.LoginStep1Response;
import com.ctg.aiFab.gateway.dto.MfaVerifyRequest;
import com.ctg.aiFab.gateway.entity.User;
import com.ctg.aiFab.gateway.service.JwtService;
import com.ctg.aiFab.gateway.service.MfaService;
import com.ctg.aiFab.gateway.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 认证控制器
 *
 * @author Developer
 * @since 2026-07-01
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final MfaService mfaService;

    // 临时存储：tempToken -> userId（实际生产环境应使用Redis）
    private final Map<String, Long> tempTokenStore = new ConcurrentHashMap<>();

    /**
     * 登录第一步：验证用户名和密码
     */
    @PostMapping("/login")
    public ApiResponse<LoginStep1Response> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.findByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ApiResponse.error(401, "用户名或密码错误");
        }

        if (user.getStatus() != 1) {
            return ApiResponse.error(403, "账号已被禁用");
        }

        LoginStep1Response response = new LoginStep1Response();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());

        // 检查是否需要MFA
        if (Boolean.TRUE.equals(user.getMfaEnabled())) {
            // 生成临时令牌
            String tempToken = UUID.randomUUID().toString();
            tempTokenStore.put(tempToken, user.getId());
            response.setTempToken(tempToken);
            response.setMfaRequired(true);
        } else {
            // 不需要MFA，直接生成JWT
            String token = jwtService.generateToken(user.getId(), user.getUsername());
            List<String> roles = userService.getRoleCodes(user.getId());
            List<String> permissions = userService.getPermissionCodes(user.getId());

            LoginResponse fullResponse = new LoginResponse();
            fullResponse.setToken(token);
            fullResponse.setUserId(user.getId());
            fullResponse.setUsername(user.getUsername());
            fullResponse.setRealName(user.getRealName());
            fullResponse.setRoles(roles);
            fullResponse.setPermissions(permissions);

            return ApiResponse.success(fullResponse);
        }

        return ApiResponse.success(response);
    }

    /**
     * 登录第二步：验证MFA代码
     */
    @PostMapping("/mfa/verify")
    public ApiResponse<LoginResponse> verifyMfa(@Valid @RequestBody MfaVerifyRequest request) {
        Long userId = tempTokenStore.get(request.getTempToken());
        if (userId == null) {
            return ApiResponse.error(401, "临时令牌无效或已过期");
        }

        User user = userService.findById(userId);
        if (user == null || !Boolean.TRUE.equals(user.getMfaEnabled())) {
            return ApiResponse.error(401, "用户状态异常");
        }

        // 验证TOTP代码
        if (!mfaService.verifyCode(user.getMfaSecret(), request.getCode())) {
            return ApiResponse.error(401, "验证码错误或已过期");
        }

        // 验证成功，清除临时令牌
        tempTokenStore.remove(request.getTempToken());

        // 生成JWT
        String token = jwtService.generateToken(user.getId(), user.getUsername());
        List<String> roles = userService.getRoleCodes(user.getId());
        List<String> permissions = userService.getPermissionCodes(user.getId());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setRoles(roles);
        response.setPermissions(permissions);

        return ApiResponse.success(response);
    }
}
