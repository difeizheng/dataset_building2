package com.ctg.dataFab.ingest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 数据样本创建请求DTO
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
@Schema(description = "数据样本创建请求")
public class DataSampleCreateRequest {

    @NotBlank(message = "数据名称不能为空")
    @Schema(description = "数据名称", example = "示例文本数据")
    private String name;

    @NotNull(message = "数据模态不能为空")
    @Schema(description = "数据模态：1-文本，2-图像，3-音频，4-视频", example = "1")
    private Integer modality;

    @NotNull(message = "数据分级不能为空")
    @Schema(description = "数据分级：1-L1公开，2-L2内部，3-L3敏感，4-L4核心", example = "2")
    private Integer dataLevel;

    @Schema(description = "数据来源", example = "用户上传")
    private String source;

    @NotBlank(message = "文件路径不能为空")
    @Schema(description = "文件路径", example = "/data/text/sample1.txt")
    private String filePath;

    @Schema(description = "文件大小（字节）", example = "1024")
    private Long fileSize;

    @Schema(description = "文件类型/MIME类型", example = "text/plain")
    private String mimeType;

    @Schema(description = "文件MD5哈希", example = "d41d8cd98f00b204e9800998ecf8427e")
    private String fileHash;

    @Schema(description = "元数据（JSON格式）")
    private String metadata;
}
