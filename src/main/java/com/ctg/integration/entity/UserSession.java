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
 * 会话实体
 * 管理用户登录会话
 *
 * @author CTG
 * @since 2026-07-01
 */
@Entity
@Table(name = "user_session", indexes = {
        @Index(name = "idx_session_user_id", columnList = "user_id"),
        @Index(name = "idx_session_token", columnList = "session_token"),
        @Index(name = "idx_session_expire", columnList = "expire_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "username", nullable = false, length = 64)
    private String username;

    @Column(name = "session_token", nullable = false, unique = true, length = 255)
    private String sessionToken;

    @Column(name = "refresh_token", length = 255)
    private String refreshToken;

    @Column(name = "client_ip", length = 50)
    private String clientIp;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    @Column(name = "login_at", nullable = false)
    private LocalDateTime loginAt;

    @Column(name = "expire_at", nullable = false)
    private LocalDateTime expireAt;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "mfa_verified", nullable = false)
    @Builder.Default
    private Boolean mfaVerified = false;

    /**
     * 判断会话是否过期
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireAt);
    }

    /**
     * 更新最后活跃时间
     */
    public void updateLastActive() {
        this.lastActiveAt = LocalDateTime.now();
    }
}
