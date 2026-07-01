package com.ctg.integration.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ctg.integration.dto.audit.AuditLogQuery;
import com.ctg.integration.dto.audit.AuditLogVO;
import com.ctg.integration.service.AuditLogService;
import com.ctg.integration.service.AuditLogService.AuditStatistics;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;

/**
 * 审计日志控制器
 * 提供审计日志查询和统计接口
 *
 * @author CTG
 * @since 2026-07-01
 */
@Slf4j
@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
@Tag(name = "审计日志", description = "审计日志查询和统计")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("/logs")
    @Operation(summary = "查询日志", description = "查询审计日志列表")
    public ResponseEntity<Page<AuditLogVO>> queryLogs(AuditLogQuery query) {
        log.debug("查询审计日志");
        Page<AuditLogVO> logs = auditLogService.queryAuditLogs(query);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/statistics")
    @Operation(summary = "统计数据", description = "获取审计统计数据")
    public ResponseEntity<AuditStatistics> getStatistics(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        log.debug("获取审计统计");
        AuditStatistics statistics = auditLogService.getStatistics(userId, startTime, endTime);
        return ResponseEntity.ok(statistics);
    }

    @PostMapping("/clean")
    @Operation(summary = "清理日志", description = "清理过期审计日志")
    public ResponseEntity<Long> cleanExpiredLogs(@RequestParam(defaultValue = "180") int retentionDays) {
        log.info("清理过期审计日志: retentionDays={}", retentionDays);
        long deleted = auditLogService.cleanExpiredLogs(retentionDays);
        return ResponseEntity.ok(deleted);
    }
}
