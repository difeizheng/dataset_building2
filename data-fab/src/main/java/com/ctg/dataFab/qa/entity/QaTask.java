package com.ctg.dataFab.qa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 质量评估任务实体
 * 对应表：t_qa_task
 * 实现 M2 四模态质量阈值检测
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
@TableName("t_qa_task")
public class QaTask {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 数据集ID
     */
    private Long datasetId;

    /**
     * 数据模态：1-文本，2-图像，3-音频，4-视频
     */
    private Integer modality;

    /**
     * 批次号
     */
    private String batch;

    /**
     * 任务状态：0-待执行，1-执行中，2-已通过，3-未通过，4-已取消
     */
    private Integer status;

    /**
     * 评估指标（JSON格式）
     */
    private String metrics;

    /**
     * 是否通过：0-否，1-是
     */
    private Integer passed;

    /**
     * 门禁检查结果（JSON数组）
     */
    private String gates;

    /**
     * 三审三校状态：0-自动预审，1-人工复审，2-专家终审，3-发布
     */
    private Integer reviewStage;

    /**
     * 评审意见
     */
    private String reviewComment;

    /**
     * 评审人
     */
    private String reviewer;

    /**
     * 评审时间
     */
    private LocalDateTime reviewTime;

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
