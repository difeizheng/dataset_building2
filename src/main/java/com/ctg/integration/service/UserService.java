package com.ctg.integration.service;

import java.util.List;

import com.ctg.integration.dto.user.*;

/**
 * 用户管理服务接口
 *
 * @author CTG
 * @since 2026-07-01
 */
public interface UserService {

    /**
     * 创建用户
     */
    UserVO createUser(CreateUserRequest request);

    /**
     * 更新用户
     */
    UserVO updateUser(Long userId, UpdateUserRequest request);

    /**
     * 删除用户
     */
    void deleteUser(Long userId);

    /**
     * 获取用户详情
     */
    UserVO getUserById(Long userId);

    /**
     * 获取用户列表
     */
    List<UserVO> listUsers(String department, Boolean enabled);

    /**
     * 启用/禁用用户
     */
    void toggleUserStatus(Long userId, Boolean enabled);

    /**
     * 分配角色
     */
    void assignRoles(Long userId, List<Long> roleIds);
}
