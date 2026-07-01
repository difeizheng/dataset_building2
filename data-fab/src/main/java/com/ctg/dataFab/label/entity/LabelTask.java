package com.ctg.dataFab.label.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 标注任务实体
 * 对应表：t_label_task
 * 支持 M3 IAA 双盲标注 + 仲裁机制
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
@TableName("t_label_task")
public class LabelTask {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 数据集ID
     */
    private Long datasetId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 数据模态：1-文本，2-图像，3-音频，4-视频
     */
    private Integer modality;

    /**
     * 标注类型：分类/实体识别/关系抽取/目标检测/语义分割/语音转写/事件标记/目标跟踪/行为识别
     */
    private String labelType;

    /**
     * 标注Schema（JSON格式，定义标签体系）
     */
    private String labelSchema;

    /**
     * 是否双盲标注：0-否，1-是
     */
    private Integer doubleBlind;

    /**
     * 标注员数量（双盲时>=2）
     */
    private Integer annotatorCount;

    /**
     * 标注员ID列表（JSON数组）
     */
    private String annotatorIds;

    /**
     * 仲裁员ID
     */
    private Long arbitratorId;

    /**
     * 任务状态：0-草稿，1-待分配，2-标注中，3-仲裁中，4-已完成，5-已驳回，6-已取消
     */
    private Integer status;

    /**
     * 总样本数
     */
    private Integer totalSamples;

    /**
     * 已标注数
     */
    private Integer labeledCount;

    /**
     * IAA Kappa系数
     */
    private Double kappaScore;

    /**
     * SOP说明
     */
    private String sopDescription;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;
}
