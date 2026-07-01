package com.ctg.integration.dto.integration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI中台响应DTO
 *
 * @author CTG
 * @since 2026-07-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIPlatformResponse {

    private boolean success;
    private String taskId;
    private String result;
    private String errorMessage;
    private Long duration;
    private String modelId;
}
