package com.ctg.integration.dto.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审计日志查询条件
 *
 * @author CTG
 * @since 2026-07-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogQuery {

    private Long userId;
    private String username;
    private String operation;
    private String resourceType;
    private String startTime;
    private String endTime;
    private String result;
    private String dataLevel;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;
}
