package com.ctg.integration.dto.integration;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 三峡行云请求DTO
 *
 * @author CTG
 * @since 2026-07-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XingyunRequest {

    @NotBlank(message = "流程ID不能为空")
    private String processId;

    @NotBlank(message = "操作类型不能为空")
    private String operationType;

    private Map<String, Object> formData;

    private String approvalComment;
}
