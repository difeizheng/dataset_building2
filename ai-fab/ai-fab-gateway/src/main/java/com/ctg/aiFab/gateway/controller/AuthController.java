package com.ctg.aiFab.gateway.controller;

import com.ctg.aiFab.gateway.common.dto.ApiResponse;
import com.ctg.aiFab.gateway.dto.LoginRequest;
import com.ctg.aiFab.gateway.dto.LoginResponse;
import com.ctg.aiFab.gateway.entity.User;
import com.ctg.aiFab.gateway.service.JwtService;
import com.ctg.aiFab.gateway.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.findByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ApiResponse.error(401, "用户名或密码错误");
        }

        if (user.getStatus() != 1) {
            return ApiResponse.error(403, "账号已被禁用");
        }

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
