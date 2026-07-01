package com.ctg.dataFab.delivery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 数据集交付记录实体
 * 对应表：t_delivery_record
 * 支持 FAIR / Croissant / 版本管理
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
@TableName("t_delivery_record")
public class DeliveryRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 数据集ID
     */
    private Long datasetId;

    /**
     * 版本号
     */
    private String version;

    /**
     * 数据集URI
     */
    private String datasetUri;

    /**
     * 许可证
     */
    private String license;

    /**
     * 数据血缘（JSON格式）
     */
    private String lineage;

    /**
     * 访问策略（JSON格式）
     */
    private String accessPolicy;

    /**
     * FAIR元数据（JSON格式）
     */
    private String fairMetadata;

    /**
     * Croissant元数据（JSON格式）
     */
    private String croissantMetadata;

    /**
     * 发布状态：0-草稿，1-已发布，2-已下架
     */
    private Integer status;

    /**
     * 下载次数
     */
    private Integer downloadCount;

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
