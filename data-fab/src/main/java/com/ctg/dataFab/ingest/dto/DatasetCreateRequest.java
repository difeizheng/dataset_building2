package com.ctg.dataFab.ingest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 数据集创建请求DTO
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
@Schema(description = "数据集创建请求")
public class DatasetCreateRequest {

    @NotBlank(message = "数据集名称不能为空")
    @Schema(description = "数据集名称", example = "三峡设备文本数据集")
    private String name;

    @Schema(description = "数据集描述", example = "用于设备故障诊断的文本数据集")
    private String description;

    @NotNull(message = "数据模态不能为空")
    @Schema(description = "数据模态：1-文本，2-图像，3-音频，4-视频", example = "1")
    private Integer modality;

    @NotNull(message = "数据分级不能为空")
    @Schema(description = "数据分级：1-L1公开，2-L2内部，3-L3敏感，4-L4核心", example = "2")
    private Integer dataLevel;

    @Schema(description = "版本号", example = "1.0.0")
    private String version;

    @Schema(description = "标签（JSON数组）", example = "[\"设备\",\"故障诊断\"]")
    private String tags;
}
