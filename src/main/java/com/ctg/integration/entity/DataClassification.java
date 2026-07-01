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
 * 数据分级实体
 * 数据安全分级管控（L1-L4）
 *
 * @author CTG
 * @since 2026-07-01
 */
@Entity
@Table(name = "data_classification", indexes = {
        @Index(name = "idx_data_class_level", columnList = "security_level")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataClassification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 数据分类名称
     */
    @Column(name = "classification_name", nullable = false, length = 100)
    private String classificationName;

    /**
     * 安全级别 L1-L4
     */
    @Column(name = "security_level", nullable = false, length = 10)
    private String securityLevel;

    /**
     * 是否需要加密存储
     */
    @Column(name = "require_encryption", nullable = false)
    @Builder.Default
    private Boolean requireEncryption = false;

    /**
     * 是否需要脱敏
     */
    @Column(name = "require_masking", nullable = false)
    @Builder.Default
    private Boolean requireMasking = false;

    /**
     * 脱敏规则（JSON格式）
     */
    @Column(name = "masking_rule", columnDefinition = "TEXT")
    private String maskingRule;

    /**
     * 访问控制策略
     */
    @Column(name = "access_policy", length = 255)
    private String accessPolicy;

    /**
     * 描述
     */
    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 安全级别常量
     */
    public static final String LEVEL_L1 = "L1"; // 公开数据
    public static final String LEVEL_L2 = "L2"; // 内部数据
    public static final String LEVEL_L3 = "L3"; // 敏感数据
    public static final String LEVEL_L4 = "L4"; // 机密数据
}
