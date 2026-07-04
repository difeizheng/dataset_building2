package com.ctg.integration.dto.monitor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 告警查询条件
 *
 * @author CTG
 * @since 2026-07-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertQuery {

    private String severity;
    private String status;
    private String alertType;
    private String startTime;
    private String endTime;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;
}
