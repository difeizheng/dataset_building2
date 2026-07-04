package com.ctg.kbFab.ontology.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 本体关系定义
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
@TableName("ontology_relation")
public class OntologyRelation {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /** 关系类型名 */
    private String relationName;

    /** 源类ID */
    private String sourceClassId;

    /** 目标类ID */
    private String targetClassId;

    /** 关系描述 */
    private String description;

    /** 基数约束(1:1, 1:N, M:N) */
    private String cardinality;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
