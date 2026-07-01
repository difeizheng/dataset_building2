package com.ctg.integration.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审计日志实体
 * 记录所有关键操作，满足等保三级要求
 *
 * @author CTG
 * @since 2026-07-01
 */
@Entity
@Table(name = "audit_log", indexes = {
        @Index(name = "idx_audit_user_id", columnList = "user_id"),
        @Index(name = "idx_audit_operation", columnList = "operation"),
        @Index(name = "idx_audit_created_at", columnList = "created_at"),
        @Index(name = "idx_audit_resource_type", columnList = "resource_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 操作人ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 操作人用户名
     */
    @Column(name = "username", nullable = false, length = 64)
    private String username;

    /**
     * 操作人真实姓名
     */
    @Column(name = "real_name", length = 100)
    private String realName;

    /**
     * 操作人IP地址
     */
    @Column(name = "client_ip", length = 50)
    private String clientIp;

    /**
     * 操作人User-Agent
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * 操作类型
     */
    @Column(name = "operation", nullable = false, length = 50)
    private OperationType operation;

    /**
     * 资源类型
     */
    @Column(name = "resource_type", length = 50)
    private String resourceType;

    /**
     * 资源ID
     */
    @Column(name = "resource_id", length = 100)
    private String resourceId;

    /**
     * 资源名称
     */
    @Column(name = "resource_name", length = 255)
    private String resourceName;

    /**
     * 操作描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 请求方法
     */
    @Column(name = "request_method", length = 10)
    private String requestMethod;

    /**
     * 请求URL
     */
    @Column(name = "request_url", length = 500)
    private String requestUrl;

    /**
     * 请求参数（脱敏后）
     */
    @Column(name = "request_params", columnDefinition = "TEXT")
    private String requestParams;

    /**
     * 响应状态
     */
    @Column(name = "response_status")
    private Integer responseStatus;

    /**
     * 操作结果
     */
    @Column(name = "result", nullable = false)
    private ResultType result;

    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 执行耗时（毫秒）
     */
    @Column(name = "duration")
    private Long duration;

    /**
     * 数据分级（L1-L4）
     */
    @Column(name = "data_level", length = 10)
    private String dataLevel;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 操作类型枚举
     */
    public enum OperationType {
        // 认证相关
        LOGIN, LOGOUT, LOGIN_FAIL, PASSWORD_CHANGE,
        // 用户管理
        USER_CREATE, USER_UPDATE, USER_DELETE, USER_ENABLE, USER_DISABLE,
        // 角色管理
        ROLE_CREATE, ROLE_UPDATE, ROLE_DELETE, ROLE_ASSIGN,
        // 权限管理
        PERMISSION_CREATE, PERMISSION_UPDATE, PERMISSION_DELETE,
        // 数据操作
        DATA_CREATE, DATA_UPDATE, DATA_DELETE, DATA_QUERY, DATA_EXPORT, DATA_IMPORT,
        // 系统配置
        CONFIG_UPDATE, SYSTEM_START, SYSTEM_STOP,
        // 安全相关
        ACCESS_DENIED, DATA_MASK, DATA_ENCRYPT, DATA_DECRYPT,
        // 集成相关
        INTEGRATION_CALL, INTEGRATION_CALLBACK
    }

    /**
     * 操作结果枚举
     */
    public enum ResultType {
        SUCCESS, FAILURE
    }
}
