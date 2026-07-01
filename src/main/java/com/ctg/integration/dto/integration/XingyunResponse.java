package com.ctg.integration.dto.integration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 三峡行云响应DTO
 *
 * @author CTG
 * @since 2026-07-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XingyunResponse {

    private boolean success;
    private String approvalId;
    private String status;
    private String errorMessage;
    private Long duration;
}
