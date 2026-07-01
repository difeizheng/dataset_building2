package com.ctg.integration.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 权限实体
 *
 * @author CTG
 * @since 2026-07-01
 */
@Entity
@Table(name = "sys_permission")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 权限类型
     */
    @Column(name = "permission_type", nullable = false, length = 20)
    private PermissionType type;

    /**
     * 资源路径（如 /api/users/**）
     */
    @Column(length = 255)
    private String resource;

    /**
     * HTTP方法（GET, POST, PUT, DELETE等）
     */
    @Column(length = 50)
    private String method;

    @Column(length = 255)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum PermissionType {
        MENU,       // 菜单权限
        BUTTON,     // 按钮权限
        API,        // API权限
        DATA        // 数据权限
    }
}
