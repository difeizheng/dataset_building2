package com.ctg.dataFab.common.dto;

import lombok.Data;
import java.util.List;

/**
 * 分页响应
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
public class PageResponse<T> {

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Integer pages;

    /**
     * 构建分页响应
     */
    public static <T> PageResponse<T> of(List<T> records, Long total, Integer pageNum, Integer pageSize) {
        PageResponse<T> response = new PageResponse<>();
        response.setRecords(records);
        response.setTotal(total);
        response.setPageNum(pageNum);
        response.setPageSize(pageSize);
        response.setPages((int) Math.ceil((double) total / pageSize));
        return response;
    }
}
