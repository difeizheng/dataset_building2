package com.ctg.integration.dto.integration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 大数据平台响应DTO
 *
 * @author CTG
 * @since 2026-07-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BigDataResponse {

    private boolean success;
    private String taskId;
    private String resultData;
    private Long recordCount;
    private String errorMessage;
    private Long duration;
}
