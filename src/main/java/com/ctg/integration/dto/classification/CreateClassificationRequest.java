package com.ctg.integration.dto.classification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建数据分级请求DTO
 *
 * @author CTG
 * @since 2026-07-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateClassificationRequest {

    @NotBlank(message = "分类名称不能为空")
    private String classificationName;

    @NotBlank(message = "安全级别不能为空")
    @Pattern(regexp = "^L[1-4]$", message = "安全级别必须是L1-L4")
    private String securityLevel;

    private Boolean requireEncryption;
    private Boolean requireMasking;
    private String maskingRule;
    private String accessPolicy;
    private String description;
}
