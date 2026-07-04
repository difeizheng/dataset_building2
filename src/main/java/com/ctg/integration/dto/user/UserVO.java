package com.ctg.integration.dto.user;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户视图对象
 *
 * @author CTG
 * @since 2026-07-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {

    private Long id;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private String employeeId;
    private String department;
    private Boolean enabled;
    private LocalDateTime lastLoginAt;
    private List<RoleVO> roles;
    private LocalDateTime createdAt;
}
