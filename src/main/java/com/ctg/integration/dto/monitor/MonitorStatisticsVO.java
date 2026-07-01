package com.ctg.integration.dto.monitor;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 监控统计视图对象
 *
 * @author CTG
 * @since 2026-07-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorStatisticsVO {

    private Long totalAlerts;
    private Long activeAlerts;
    private Long resolvedAlerts;
    private Double avgResponseTime;
    private Double systemUptime;
    private List<AlertCountByType> alertCountsByType;
    private List<AlertCountBySeverity> alertCountsBySeverity;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertCountByType {
        private String type;
        private Long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertCountBySeverity {
        private String severity;
        private Long count;
    }
}
