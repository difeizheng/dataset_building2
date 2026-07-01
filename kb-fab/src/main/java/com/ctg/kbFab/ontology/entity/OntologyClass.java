package com.ctg.kbFab.ontology.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 本体类实体
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
@TableName("ontology_class")
public class OntologyClass {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /** 类名 */
    private String className;

    /** 父类ID */
    private String parentClassId;

    /** 描述 */
    private String description;

    /** 属性定义JSON */
    private String propertiesJson;

    /** 约束规则JSON */
    private String constraintsJson;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
