package com.ctg.integration.dto.classification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新数据分级请求DTO
 *
 * @author CTG
 * @since 2026-07-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateClassificationRequest {

    private String classificationName;
    private Boolean requireEncryption;
    private Boolean requireMasking;
    private String maskingRule;
    private String accessPolicy;
    private String description;
}
