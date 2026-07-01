package com.ctg.dataFab.common.dto;

import lombok.Data;

/**
 * 分页查询请求
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
public class PageRequest {

    /**
     * 页码（从1开始）
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 20;

    /**
     * 排序字段
     */
    private String orderBy;

    /**
     * 排序方向（asc/desc）
     */
    private String orderDirection = "desc";

    /**
     * 计算偏移量
     */
    public Integer getOffset() {
        return (pageNum - 1) * pageSize;
    }
}
