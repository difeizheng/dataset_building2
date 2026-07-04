package com.ctg.integration.dto.integration;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * AI中台请求DTO
 *
 * @author CTG
 * @since 2026-07-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIPlatformRequest {

    @NotBlank(message = "模型ID不能为空")
    private String modelId;

    @NotBlank(message = "任务类型不能为空")
    private String taskType;

    private Map<String, Object> parameters;

    private String inputData;

    private Integer timeout;
}
