package com.ctg.integration.dto.classification;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据分级视图对象
 *
 * @author CTG
 * @since 2026-07-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataClassificationVO {

    private Long id;
    private String classificationName;
    private String securityLevel;
    private Boolean requireEncryption;
    private Boolean requireMasking;
    private String maskingRule;
    private String accessPolicy;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
