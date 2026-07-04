package com.ctg.aiFab.gateway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * MFA验证请求
 *
 * @author Developer
 * @since 2026-07-02
 */
@Data
public class MfaVerifyRequest {

    /** 临时令牌（从第一步登录获取） */
    @NotBlank(message = "临时令牌不能为空")
    private String tempToken;

    /** 6位TOTP验证码 */
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码必须是6位数字")
    private String code;
}
