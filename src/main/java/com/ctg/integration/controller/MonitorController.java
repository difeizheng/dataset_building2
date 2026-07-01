package com.ctg.integration.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ctg.integration.dto.monitor.*;
import com.ctg.integration.service.MonitorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 监控控制器
 * 提供系统健康检查、性能指标、告警管理等接口
 *
 * @author CTG
 * @since 2026-07-01
 */
@Slf4j
@RestController
@RequestMapping("/monitor")
@RequiredArgsConstructor
@Tag(name = "运维监控", description = "系统健康检查、性能指标、告警管理")
public class MonitorController {

    private final MonitorService monitorService;

    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "获取系统健康状态")
    public ResponseEntity<HealthStatusVO> getHealth() {
        log.debug("收到健康检查请求");
        HealthStatusVO status = monitorService.getHealthStatus();
        return ResponseEntity.ok(status);
    }

    @GetMapping("/metrics")
    @Operation(summary = "获取指标", description = "获取性能指标数据")
    public ResponseEntity<List<MetricVO>> getMetrics(
            @RequestParam(required = false) String metricType) {
        log.debug("获取性能指标: type={}", metricType);
        List<MetricVO> metrics = monitorService.getMetrics(metricType);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/resources")
    @Operation(summary = "系统资源", description = "获取系统资源使用情况")
    public ResponseEntity<SystemResourceVO> getResources() {
        log.debug("获取系统资源使用情况");
        SystemResourceVO resources = monitorService.getSystemResources();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/alerts")
    @Operation(summary = "告警列表", description = "获取告警列表")
    public ResponseEntity<List<AlertVO>> getAlerts(AlertQuery query) {
        log.debug("获取告警列表");
        List<AlertVO> alerts = monitorService.getAlerts(query);
        return ResponseEntity.ok(alerts);
    }

    @PostMapping("/alerts/{id}/acknowledge")
    @Operation(summary = "确认告警", description = "确认指定告警")
    public ResponseEntity<Void> acknowledgeAlert(
            @PathVariable Long id,
            @RequestParam String operator) {
        log.info("确认告警: id={}, operator={}", id, operator);
        monitorService.acknowledgeAlert(id, operator);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/statistics")
    @Operation(summary = "监控统计", description = "获取监控统计数据")
    public ResponseEntity<MonitorStatisticsVO> getStatistics(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        log.debug("获取监控统计");
        MonitorStatisticsVO statistics = monitorService.getStatistics(startTime, endTime);
        return ResponseEntity.ok(statistics);
    }
}
