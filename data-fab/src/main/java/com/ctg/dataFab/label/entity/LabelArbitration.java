package com.ctg.dataFab.label.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 仲裁记录实体
 * 对应表：t_label_arbitration
 * 当 Kappa 在 0.70~0.85 之间时进入仲裁
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
@TableName("t_label_arbitration")
public class LabelArbitration {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 标注任务ID
     */
    private Long taskId;

    /**
     * 数据样本ID
     */
    private Long sampleId;

    /**
     * 原始Kappa值
     */
    private Double originalKappa;

    /**
     * 冲突标注（JSON格式，包含各标注员结果）
     */
    private String conflictAnnotations;

    /**
     * 仲裁员ID
     */
    private Long arbitratorId;

    /**
     * 仲裁结果（JSON格式）
     */
    private String finalAnnotations;

    /**
     * 仲裁状态：0-待仲裁，1-已仲裁
     */
    private Integer status;

    /**
     * 仲裁时间
     */
    private LocalDateTime arbitrateTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
