package com.ctg.aiFab.gateway.service;

import com.ctg.aiFab.gateway.entity.User;
import com.ctg.aiFab.gateway.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务
 *
 * @author Developer
 * @since 2026-07-01
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    public User findById(Long id) {
        return userMapper.selectById(id);
    }

    public List<String> getRoleCodes(Long userId) {
        return userMapper.findRoleCodesByUserId(userId);
    }

    public List<String> getPermissionCodes(Long userId) {
        return userMapper.findPermissionCodesByUserId(userId);
    }

    public void updateMfaSecret(Long userId, String secret) {
        User user = new User();
        user.setId(userId);
        user.setMfaSecret(secret);
        user.setMfaEnabled(true);
        userMapper.updateById(user);
    }
}
