package com.ctg.kbFab.quality.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 质量评估记录
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
@TableName("quality_evaluation")
public class QualityEvaluation {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /** 知识条目ID */
    private String knowledgeId;

    /** 完整性得分(0-1) */
    private Double completenessScore;

    /** 一致性得分(0-1) */
    private Double consistencyScore;

    /** 准确性得分(0-1) */
    private Double accuracyScore;

    /** 综合得分(0-1) */
    private Double overallScore;

    /** 是否通过 */
    private Boolean passed;

    /** 评估详情JSON */
    private String detailJson;

    /** 评估者（系统/用户ID） */
    private String evaluator;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
