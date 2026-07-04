package com.ctg.kbFab.extraction.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.ctg.kbFab.common.enums.ExtractionStatus;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 抽取任务实体
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
@TableName("extraction_task")
public class ExtractionTask {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /** 任务名称 */
    private String taskName;

    /** 输入文本 */
    @TableField("input_text")
    private String inputText;

    /** 抽取类型：NER/RELATION/ATTRIBUTE/EVENT */
    private String extractionType;

    /** 任务状态 */
    @TableField("status")
    private ExtractionStatus status;

    /** 抽取结果JSON */
    private String resultJson;

    /** 错误信息 */
    private String errorMessage;

    /** 处理耗时(ms) */
    private Long durationMs;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
