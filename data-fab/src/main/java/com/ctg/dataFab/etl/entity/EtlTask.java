package com.ctg.dataFab.etl.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 清洗任务实体
 * 对应表：t_etl_task
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
@TableName("t_etl_task")
public class EtlTask {

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
     * 任务状态：0-待执行，1-执行中，2-已完成，3-失败
     */
    private Integer status;

    /**
     * 处理的样本数量
     */
    private Integer processedCount;

    /**
     * 成功的样本数量
     */
    private Integer successCount;

    /**
     * 失败的样本数量
     */
    private Integer failedCount;

    /**
     * 执行开始时间
     */
    private LocalDateTime startTime;

    /**
     * 执行结束时间
     */
    private LocalDateTime endTime;

    /**
     * 错误信息
     */
    private String errorMessage;

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
