package com.ctg.aiFab.gateway.dto;

import lombok.Data;

/**
 * AI问答响应DTO
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
public class ChatResponse {

    private String answer;
    private String model;
    private Integer tokensUsed;
    private Long latencyMs;
}
