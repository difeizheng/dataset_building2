package com.ctg.integration.service;

import java.util.List;

import com.ctg.integration.dto.audit.AuditLogQuery;
import com.ctg.integration.dto.audit.AuditLogVO;
import com.ctg.integration.entity.AuditLog;

import org.springframework.data.domain.Page;

/**
 * 审计日志服务接口
 *
 * @author CTG
 * @since 2026-07-01
 */
public interface AuditLogService {

    /**
     * 记录审计日志（异步）
     */
    void recordAuditLog(AuditLog auditLog);

    /**
     * 查询审计日志
     */
    Page<AuditLogVO> queryAuditLogs(AuditLogQuery query);

    /**
     * 获取用户操作统计
     */
    AuditStatistics getStatistics(Long userId, String startTime, String endTime);

    /**
     * 清理过期日志
     */
    long cleanExpiredLogs(int retentionDays);

    /**
     * 审计统计
     */
    record AuditStatistics(
            long totalOperations,
            long successCount,
            long failureCount,
            List<OperationCount> operationCounts) {}

    record OperationCount(String operation, long count) {}
}
