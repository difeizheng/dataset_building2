package com.ctg.kbFab.common.dto;

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
    private List<T> records;
    private Long total;
    private Integer page;
    private Integer size;
    private Integer pages;

    public static <T> PageResponse<T> of(List<T> records, Long total, Integer page, Integer size) {
        PageResponse<T> response = new PageResponse<>();
        response.setRecords(records);
        response.setTotal(total);
        response.setPage(page);
        response.setSize(size);
        response.setPages((int) Math.ceil((double) total / size));
        return response;
    }
}
