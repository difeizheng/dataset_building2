package com.ctg.kbFab.version.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 知识版本记录
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
@TableName("knowledge_version")
public class KnowledgeVersion {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /** 知识条目ID */
    private String knowledgeId;

    /** 版本号 */
    private Integer versionNumber;

    /** 版本标题 */
    private String title;

    /** 版本内容快照 */
    private String contentSnapshot;

    /** 变更说明 */
    private String changeNote;

    /** 操作者 */
    private String operator;

    /** 操作类型：CREATE/UPDATE/PUBLISH/ARCHIVE */
    private String operationType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
