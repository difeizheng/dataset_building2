package com.ctg.integration.dto.integration;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 大数据平台请求DTO
 *
 * @author CTG
 * @since 2026-07-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BigDataRequest {

    @NotBlank(message = "数据源ID不能为空")
    private String dataSourceId;

    @NotBlank(message = "操作类型不能为空")
    private String operationType;

    private Map<String, Object> queryParameters;

    private String dataPayload;
}
