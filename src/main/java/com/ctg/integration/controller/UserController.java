package com.ctg.integration.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ctg.integration.dto.user.*;
import com.ctg.integration.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户管理控制器
 * 提供用户CRUD和角色分配接口
 *
 * @author CTG
 * @since 2026-07-01
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户CRUD和角色分配")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "创建用户", description = "创建新用户")
    public ResponseEntity<UserVO> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("创建用户: {}", request.getUsername());
        UserVO user = userService.createUser(request);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户", description = "更新用户信息")
    public ResponseEntity<UserVO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("更新用户: {}", id);
        UserVO user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "删除指定用户")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("删除用户: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取用户", description = "获取用户详情")
    public ResponseEntity<UserVO> getUser(@PathVariable Long id) {
        log.debug("获取用户: {}", id);
        UserVO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    @Operation(summary = "用户列表", description = "获取用户列表")
    public ResponseEntity<List<UserVO>> listUsers(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Boolean enabled) {
        log.debug("获取用户列表");
        List<UserVO> users = userService.listUsers(department, enabled);
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "切换状态", description = "启用/禁用用户")
    public ResponseEntity<Void> toggleUserStatus(
            @PathVariable Long id,
            @RequestParam Boolean enabled) {
        log.info("切换用户状态: id={}, enabled={}", id, enabled);
        userService.toggleUserStatus(id, enabled);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/roles")
    @Operation(summary = "分配角色", description = "为用户分配角色")
    public ResponseEntity<Void> assignRoles(
            @PathVariable Long id,
            @RequestBody List<Long> roleIds) {
        log.info("分配角色: userId={}, roleIds={}", id, roleIds);
        userService.assignRoles(id, roleIds);
        return ResponseEntity.ok().build();
    }
}
