package com.ctg.integration.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建用户请求DTO
 *
 * @author CTG
 * @since 2026-07-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 64, message = "用户名长度必须在3-64之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 32, message = "密码长度必须在8-32之间")
    private String password;

    @Size(max = 100, message = "真实姓名长度不能超过100")
    private String realName;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(max = 20, message = "手机号长度不能超过20")
    private String phone;

    @Size(max = 20, message = "工号长度不能超过20")
    private String employeeId;

    @Size(max = 50, message = "部门名称长度不能超过50")
    private String department;

    private List<Long> roleIds;
}
