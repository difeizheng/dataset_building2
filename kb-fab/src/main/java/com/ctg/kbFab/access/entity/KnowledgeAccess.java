package com.ctg.kbFab.access.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 知识访问权限
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
@TableName("knowledge_access")
public class KnowledgeAccess {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /** 知识条目ID */
    private String knowledgeId;

    /** 用户/角色ID */
    private String principalId;

    /** 主体类型：USER / ROLE / DEPT */
    private String principalType;

    /** 权限级别：READ / WRITE / ADMIN */
    private String accessLevel;

    /** 是否允许 */
    private Boolean allowed;

    /** 过期时间 */
    private LocalDateTime expireTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
