package com.ctg.integration.dto.integration;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WPS服务请求DTO
 *
 * @author CTG
 * @since 2026-07-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WPSRequest {

    @NotBlank(message = "操作类型不能为空")
    private String operationType;

    private String fileUrl;

    private String fileContent;

    private String outputFormat;
}
