package com.ctg.integration.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 告警实体
 *
 * @author CTG
 * @since 2026-07-01
 */
@Entity
@Table(name = "monitor_alert", indexes = {
        @Index(name = "idx_alert_severity", columnList = "severity"),
        @Index(name = "idx_alert_status", columnList = "status"),
        @Index(name = "idx_alert_triggered_at", columnList = "triggered_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alert_name", nullable = false, length = 100)
    private String alertName;

    @Column(name = "alert_type", nullable = false, length = 50)
    private String alertType;

    @Column(name = "severity", nullable = false, length = 20)
    private String severity;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "message", length = 500)
    private String message;

    @Column(name = "source", length = 100)
    private String source;

    @Column(name = "metric_name", length = 100)
    private String metricName;

    @Column(name = "metric_value")
    private Double metricValue;

    @Column(name = "threshold")
    private Double threshold;

    @Column(name = "triggered_at", nullable = false)
    private LocalDateTime triggeredAt;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "acknowledged_by", length = 64)
    private String acknowledgedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public enum Severity {
        INFO, WARNING, CRITICAL, FATAL
    }

    public enum Status {
        TRIGGERED, ACKNOWLEDGED, RESOLVED
    }
}
