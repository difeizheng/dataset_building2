package com.ctg.integration.dto.monitor;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 告警视图对象
 *
 * @author CTG
 * @since 2026-07-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertVO {

    private Long id;
    private String alertName;
    private String alertType;
    private String severity;
    private String status;
    private String message;
    private String source;
    private String metricName;
    private Double metricValue;
    private Double threshold;
    private LocalDateTime triggeredAt;
    private LocalDateTime acknowledgedAt;
    private String acknowledgedBy;
    private LocalDateTime resolvedAt;
}
