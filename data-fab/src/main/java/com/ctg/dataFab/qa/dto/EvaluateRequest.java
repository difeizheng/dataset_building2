package com.ctg.dataFab.qa.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Map;

/**
 * 质量评估请求
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
@Schema(description = "质量评估请求")
public class EvaluateRequest {

    @NotNull(message = "数据集ID不能为空")
    @Schema(description = "数据集ID")
    private Long datasetId;

    @NotNull(message = "数据模态不能为空")
    @Schema(description = "数据模态：1-文本，2-图像，3-音频，4-视频")
    private Integer modality;

    @Schema(description = "批次号")
    private String batch;

    @NotNull(message = "评估指标不能为空")
    @Schema(description = "评估指标（JSON格式）")
    private Map<String, Object> metrics;
}
