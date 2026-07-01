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

    /** 页码（从1开始） */
    private Integer pageNum = 1;

    /** 每页大小 */
    private Integer pageSize = 20;

    /** 排序字段 */
    private String orderBy;

    /** 排序方向（asc/desc） */
    private String orderDirection = "desc";

    /**
     * M-3: 分页大小封顶100，防止DoS
     */
    public void setPageSize(Integer pageSize) {
        if (pageSize != null && pageSize > 100) {
            this.pageSize = 100;
        } else if (pageSize != null && pageSize < 1) {
            this.pageSize = 1;
        } else {
            this.pageSize = pageSize;
        }
    }

    /**
     * 计算偏移量
     */
    public Integer getOffset() {
        return (pageNum - 1) * (pageSize != null ? pageSize : 20);
    }
}
