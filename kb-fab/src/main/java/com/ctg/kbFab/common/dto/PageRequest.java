package com.ctg.kbFab.common.dto;

import lombok.Data;

/**
 * 分页请求
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
public class PageRequest {
    private Integer page = 1;
    private Integer size = 20;
    private String sortBy;
    private String sortOrder = "asc";
}
