package com.ctg.integration.dto.integration;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 大模型平台请求DTO
 *
 * @author CTG
 * @since 2026-07-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LLMRequest {

    @NotBlank(message = "提示词不能为空")
    private String prompt;

    private String model;

    private Integer maxTokens;

    private Double temperature;

    private String systemPrompt;
}
