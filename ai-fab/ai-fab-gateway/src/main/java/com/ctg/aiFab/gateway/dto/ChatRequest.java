package com.ctg.aiFab.gateway.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI问答请求DTO
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
public class ChatRequest {

    @NotBlank(message = "问题不能为空")
    private String question;

    private String model; // 可选：gpt-4, claude-3, etc.

    private Double temperature = 0.7;

    private Integer maxTokens = 2000;
}
