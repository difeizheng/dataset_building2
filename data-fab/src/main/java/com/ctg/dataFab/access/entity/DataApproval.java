package com.ctg.dataFab.access.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 数据审批实体
 * 对应表：t_data_approval
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
@TableName("t_data_approval")
public class DataApproval {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 审批单号 (业务ID) */
    private String approvalNo;

    /** 资源ID (样本ID/数据集ID) */
    private Long resourceId;

    /** 资源类型: SAMPLE, DATASET */
    private String resourceType;

    /** 操作类型: DOWNLOAD, PRINT, EXPORT */
    private String action;

    /** 申请人ID */
    private Long applicantId;

    /** 审批人ID */
    private Long approverId;

    /** 审批状态: 0-待审批, 1-已通过, 2-已拒绝 */
    private Integer status;

    /** 审批意见 */
    private String comment;

    /** 审批时间 */
    private LocalDateTime approveTime;

    /** 过期时间 */
    private LocalDateTime expireTime;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
