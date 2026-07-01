package com.ctg.dataFab.label.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 标注记录实体
 * 对应表：t_label_record
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
@TableName("t_label_record")
public class LabelRecord {

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
     * 标注员ID
     */
    private Long annotatorId;

    /**
     * 标注结果（JSON格式）
     */
    private String annotations;

    /**
     * 标注状态：0-待标注，1-已标注，2-已审核
     */
    private Integer status;

    /**
     * 标注耗时（秒）
     */
    private Long durationSeconds;

    /**
     * 提交时间
     */
    private LocalDateTime submitTime;

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
