package com.ctg.dataFab.delivery.dto;

import lombok.Data;

/**
 * 数据集发布响应
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
public class PublishResponse {

    private Long recordId;
    private String datasetUri;
    private String license;
    private String lineage;
}
