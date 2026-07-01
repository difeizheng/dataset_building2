package com.ctg.dataFab.etl.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 清洗规则实体
 * 对应表：t_etl_rule
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
@TableName("t_etl_rule")
public class EtlRule {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 规则名称
     */
    private String name;

    /**
     * 规则描述
     */
    private String description;

    /**
     * 数据模态：1-文本，2-图像，3-音频，4-视频
     */
    private Integer modality;

    /**
     * 规则类型：去重/标准化/脱敏/分级等
     */
    private String ruleType;

    /**
     * 规则配置（JSON格式）
     */
    private String ruleConfig;

    /**
     * 优先级（数字越大优先级越高）
     */
    private Integer priority;

    /**
     * 是否启用：0-禁用，1-启用
     */
    private Integer enabled;

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
