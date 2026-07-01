package com.ctg.dataFab.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 数据集发布请求
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
@Schema(description = "数据集发布请求")
public class PublishRequest {

    @NotNull(message = "数据集ID不能为空")
    @Schema(description = "数据集ID")
    private Long datasetId;

    @NotBlank(message = "版本号不能为空")
    @Schema(description = "版本号", example = "1.0.0")
    private String version;

    @Schema(description = "Croissant元数据（JSON格式）")
    private String croissant;

    @Schema(description = "访问策略（JSON格式）")
    private String accessPolicy;

    @Schema(description = "许可证", example = "Apache-2.0")
    private String license;
}
