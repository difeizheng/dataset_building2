package com.ctg.kbFab.storage.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.ctg.kbFab.common.enums.KnowledgeStatus;
import com.ctg.kbFab.common.enums.KnowledgeType;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 知识条目实体（存储在达梦数据库中）
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
@TableName("knowledge_entry")
public class KnowledgeEntry {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /** 知识标题 */
    private String title;

    /** 知识内容 */
    private String content;

    /** 知识类型 */
    @TableField("knowledge_type")
    private KnowledgeType knowledgeType;

    /** 知识状态 */
    @TableField("status")
    private KnowledgeStatus status;

    /** 来源文档ID */
    private String sourceDocId;

    /** 来源文档名 */
    private String sourceDocName;

    /** 所属领域 */
    private String domain;

    /** 数据分级：L1/L2/L3/L4 */
    private String dataLevel;

    /** 标签JSON */
    private String tagsJson;

    /** 向量ID（对应向量库中的ID） */
    private String vectorId;

    /** 图谱节点ID（对应图库中的ID） */
    private String graphNodeId;

    /** 版本号 */
    private Integer version;

    /** 创建者 */
    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
