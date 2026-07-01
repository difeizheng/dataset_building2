package com.ctg.integration.dto.integration;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 签章系统请求DTO
 *
 * @author CTG
 * @since 2026-07-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignatureRequest {

    @NotBlank(message = "文档ID不能为空")
    private String documentId;

    @NotBlank(message = "签章人ID不能为空")
    private String signerId;

    private String signatureType;

    private String signaturePosition;
}
