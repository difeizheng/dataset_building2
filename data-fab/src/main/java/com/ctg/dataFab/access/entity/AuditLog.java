package com.ctg.dataFab.access.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审计日志实体 (WORM - Write Once Read Many)
 * 对应表：t_audit_log
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
@TableName("t_audit_log")
public class AuditLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 资源ID (样本ID/数据集ID等) */
    private Long resourceId;

    /** 资源类型: SAMPLE, DATASET, DELIVERY */
    private String resourceType;

    /** 操作类型: DOWNLOAD, PRINT, EXPORT, VIEW */
    private String action;

    /** 用户ID */
    private Long userId;

    /** 操作结果: ALLOWED, BLOCKED, PENDING_APPROVAL */
    private String result;

    /** 描述 */
    private String description;

    /** IP地址 */
    private String ipAddress;

    /** 创建时间 (不可修改) */
    @TableField(fill = FieldFill.INSERT, update = "")
    private LocalDateTime createTime;
}
