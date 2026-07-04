package com.ctg.aiFab.gateway.dto;

import lombok.Data;
import java.util.List;

/**
 * 登录响应DTO
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
public class LoginResponse {

    private String token;
    private Long userId;
    private String username;
    private String realName;
    private List<String> roles;
    private List<String> permissions;
}
