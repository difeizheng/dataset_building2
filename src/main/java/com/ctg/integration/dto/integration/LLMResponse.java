package com.ctg.integration.dto.integration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 大模型平台响应DTO
 *
 * @author CTG
 * @since 2026-07-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LLMResponse {

    private boolean success;
    private String completion;
    private Integer tokensUsed;
    private String errorMessage;
    private Long duration;
}
