package com.ctg.dataFab.label.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 提交标注请求
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
@Schema(description = "提交标注请求")
public class SubmitLabelRequest {

    @NotNull(message = "任务ID不能为空")
    @Schema(description = "标注任务ID")
    private Long taskId;

    @NotNull(message = "样本ID不能为空")
    @Schema(description = "数据样本ID")
    private Long sampleId;

    @NotNull(message = "标注员ID不能为空")
    @Schema(description = "标注员ID")
    private Long annotatorId;

    @NotBlank(message = "标注结果不能为空")
    @Schema(description = "标注结果（JSON格式）")
    private String annotations;

    @Schema(description = "标注耗时（秒）")
    private Long durationSeconds;
}
