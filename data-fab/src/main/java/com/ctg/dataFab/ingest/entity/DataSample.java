package com.ctg.dataFab.ingest.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 数据样本实体
 * 对应表：t_data_sample
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
@TableName("t_data_sample")
public class DataSample {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 数据集ID
     */
    private Long datasetId;

    /**
     * 数据名称
     */
    private String name;

    /**
     * 数据模态：1-文本，2-图像，3-音频，4-视频
     */
    private Integer modality;

    /**
     * 数据分级：1-L1公开，2-L2内部，3-L3敏感，4-L4核心
     */
    private Integer dataLevel;

    /**
     * 数据状态：0-新建，1-原始，2-已清洗，3-已标注，4-质量通过，5-质量不通过，6-已发布，7-已归档
     */
    private Integer status;

    /**
     * 数据来源
     */
    private String source;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件类型/MIME类型
     */
    private String mimeType;

    /**
     * 文件MD5哈希
     */
    private String fileHash;

    /**
     * 元数据（JSON格式）
     */
    private String metadata;

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
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableLogic
    private Integer deleted;
}
