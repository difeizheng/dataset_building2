package com.ctg.integration.dto.integration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WPS服务响应DTO
 *
 * @author CTG
 * @since 2026-07-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WPSResponse {

    private boolean success;
    private String fileId;
    private String fileUrl;
    private String errorMessage;
    private Long duration;
}
