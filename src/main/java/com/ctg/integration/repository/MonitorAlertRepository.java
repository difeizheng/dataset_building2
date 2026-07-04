package com.ctg.integration.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ctg.integration.entity.MonitorAlert;

/**
 * 告警数据访问层
 *
 * @author CTG
 * @since 2026-07-01
 */
@Repository
public interface MonitorAlertRepository extends JpaRepository<MonitorAlert, Long> {

    Page<MonitorAlert> findBySeverityOrderByTriggeredAtDesc(MonitorAlert.Severity severity, Pageable pageable);

    Page<MonitorAlert> findByStatusOrderByTriggeredAtDesc(MonitorAlert.Status status, Pageable pageable);

    Page<MonitorAlert> findByAlertTypeOrderByTriggeredAtDesc(String alertType, Pageable pageable);

    @Query("SELECT a FROM MonitorAlert a WHERE a.triggeredAt BETWEEN :startTime AND :endTime ORDER BY a.triggeredAt DESC")
    Page<MonitorAlert> findByTriggeredAtBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    List<MonitorAlert> findByStatus(MonitorAlert.Status status);

    long countByStatus(MonitorAlert.Status status);

    long countBySeverity(MonitorAlert.Severity severity);

    @Query("SELECT a.alertType, COUNT(a) FROM MonitorAlert a WHERE a.triggeredAt >= :since GROUP BY a.alertType")
    List<Object[]> countByAlertTypeSince(@Param("since") LocalDateTime since);

    @Query("SELECT a.severity, COUNT(a) FROM MonitorAlert a WHERE a.triggeredAt >= :since GROUP BY a.severity")
    List<Object[]> countBySeveritySince(@Param("since") LocalDateTime since);
}
