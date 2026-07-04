package com.ctg.integration.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ctg.integration.dto.user.*;
import com.ctg.integration.entity.AuditLog;
import com.ctg.integration.entity.Permission;
import com.ctg.integration.entity.Role;
import com.ctg.integration.entity.User;
import com.ctg.integration.exception.BusinessException;
import com.ctg.integration.repository.AuditLogRepository;
import com.ctg.integration.repository.PermissionRepository;
import com.ctg.integration.repository.RoleRepository;
import com.ctg.integration.repository.UserRepository;
import com.ctg.integration.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户管理服务实现
 *
 * @author CTG
 * @since 2026-07-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserVO createUser(CreateUserRequest request) {
        log.info("创建用户: {}", request.getUsername());

        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("USER_EXISTS", "用户名已存在: " + request.getUsername());
        }

        // 检查邮箱是否已存在
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("EMAIL_EXISTS", "邮箱已存在: " + request.getEmail());
        }

        // 创建用户实体
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .realName(request.getRealName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .employeeId(request.getEmployeeId())
                .department(request.getDepartment())
                .enabled(true)
                .loginFailCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 分配角色
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            List<Role> roles = roleRepository.findAllById(request.getRoleIds());
            user.setRoles(roles.stream().collect(Collectors.toSet()));
        }

        user = userRepository.save(user);

        // 记录审计日志
        recordAuditLog(user, AuditLog.OperationType.USER_CREATE, "创建用户: " + user.getUsername());

        return toVO(user);
    }

    @Override
    @Transactional
    public UserVO updateUser(Long userId, UpdateUserRequest request) {
        log.info("更新用户: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在: " + userId));

        // 更新字段
        if (request.getRealName() != null) {
            user.setRealName(request.getRealName());
        }
        if (request.getEmail() != null) {
            if (userRepository.existsByEmail(request.getEmail()) && !request.getEmail().equals(user.getEmail())) {
                throw new BusinessException("EMAIL_EXISTS", "邮箱已存在: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getDepartment() != null) {
            user.setDepartment(request.getDepartment());
        }

        // 更新角色
        if (request.getRoleIds() != null) {
            List<Role> roles = roleRepository.findAllById(request.getRoleIds());
            user.setRoles(roles.stream().collect(Collectors.toSet()));
        }

        user.setUpdatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        // 记录审计日志
        recordAuditLog(user, AuditLog.OperationType.USER_UPDATE, "更新用户: " + user.getUsername());

        return toVO(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("删除用户: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在: " + userId));

        String username = user.getUsername();
        userRepository.delete(user);

        // 记录审计日志
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .username(username)
                .operation(AuditLog.OperationType.USER_DELETE)
                .description("删除用户: " + username)
                .result(AuditLog.ResultType.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();
        auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public UserVO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在: " + userId));
        return toVO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserVO> listUsers(String department, Boolean enabled) {
        List<User> users;
        if (department != null) {
            users = userRepository.findByDepartment(department);
        } else if (enabled != null) {
            users = userRepository.findByEnabled(enabled);
        } else {
            users = userRepository.findAll();
        }
        return users.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long userId, Boolean enabled) {
        log.info("切换用户状态: userId={}, enabled={}", userId, enabled);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在: " + userId));

        user.setEnabled(enabled);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        AuditLog.OperationType operation = enabled ? AuditLog.OperationType.USER_ENABLE : AuditLog.OperationType.USER_DISABLE;
        recordAuditLog(user, operation, (enabled ? "启用" : "禁用") + "用户: " + user.getUsername());
    }

    @Override
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        log.info("分配角色: userId={}, roleIds={}", userId, roleIds);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在: " + userId));

        List<Role> roles = roleRepository.findAllById(roleIds);
        user.setRoles(roles.stream().collect(Collectors.toSet()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        recordAuditLog(user, AuditLog.OperationType.ROLE_ASSIGN, "分配角色: " + roleIds);
    }

    private UserVO toVO(User user) {
        List<RoleVO> roles = user.getRoles().stream()
                .map(this::toRoleVO)
                .collect(Collectors.toList());

        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .employeeId(user.getEmployeeId())
                .department(user.getDepartment())
                .enabled(user.getEnabled())
                .lastLoginAt(user.getLastLoginAt())
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .build();
    }

    private RoleVO toRoleVO(Role role) {
        List<PermissionVO> permissions = role.getPermissions().stream()
                .map(this::toPermissionVO)
                .collect(Collectors.toList());

        return RoleVO.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .description(role.getDescription())
                .roleType(role.getRoleType().name())
                .permissions(permissions)
                .build();
    }

    private PermissionVO toPermissionVO(Permission permission) {
        return PermissionVO.builder()
                .id(permission.getId())
                .code(permission.getCode())
                .name(permission.getName())
                .type(permission.getType().name())
                .resource(permission.getResource())
                .method(permission.getMethod())
                .build();
    }

    private void recordAuditLog(User user, AuditLog.OperationType operation, String description) {
        AuditLog auditLog = AuditLog.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .operation(operation)
                .description(description)
                .result(AuditLog.ResultType.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();
        auditLogRepository.save(auditLog);
    }
}
