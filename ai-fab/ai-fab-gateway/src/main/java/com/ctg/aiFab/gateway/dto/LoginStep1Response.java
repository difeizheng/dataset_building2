package com.ctg.aiFab.gateway.dto;

import lombok.Data;

/**
 * 登录第一步响应 - 密码验证通过后返回
 *
 * @author Developer
 * @since 2026-07-02
 */
@Data
public class LoginStep1Response {

    /** 临时令牌，用于关联MFA验证 */
    private String tempToken;

    /** 是否需要MFA验证 */
    private Boolean mfaRequired;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 真实姓名 */
    private String realName;
}
