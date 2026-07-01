package com.ctg.dataFab.label.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 * 创建标注任务请求
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
@Schema(description = "创建标注任务请求")
public class CreateLabelTaskRequest {

    @NotNull(message = "数据集ID不能为空")
    @Schema(description = "数据集ID")
    private Long datasetId;

    @NotBlank(message = "任务名称不能为空")
    @Schema(description = "任务名称")
    private String taskName;

    @NotNull(message = "数据模态不能为空")
    @Schema(description = "数据模态：1-文本，2-图像，3-音频，4-视频")
    private Integer modality;

    @NotBlank(message = "标注类型不能为空")
    @Schema(description = "标注类型", example = "classification")
    private String labelType;

    @NotBlank(message = "标注Schema不能为空")
    @Schema(description = "标注Schema（JSON格式）")
    private String labelSchema;

    @Schema(description = "是否双盲标注", defaultValue = "true")
    private Integer doubleBlind = 1;

    @Schema(description = "标注员ID列表")
    private List<Long> annotatorIds;

    @Schema(description = "仲裁员ID")
    private Long arbitratorId;

    @Schema(description = "SOP说明")
    private String sopDescription;
}
