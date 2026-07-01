package com.ctg.integration.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ctg.integration.dto.audit.AuditLogQuery;
import com.ctg.integration.dto.audit.AuditLogVO;
import com.ctg.integration.entity.AuditLog;
import com.ctg.integration.repository.AuditLogRepository;
import com.ctg.integration.service.AuditLogService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 审计日志服务实现
 * 支持异步写入、分页查询、统计分析和日志清理
 *
 * @author CTG
 * @since 2026-07-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Async("auditLogExecutor")
    @Transactional
    public void recordAuditLog(AuditLog auditLog) {
        try {
            if (auditLog.getCreatedAt() == null) {
                auditLog.setCreatedAt(LocalDateTime.now());
            }
            auditLogRepository.save(auditLog);
            log.debug("审计日志已记录: operation={}, user={}", auditLog.getOperation(), auditLog.getUsername());
        } catch (Exception e) {
            log.error("审计日志记录失败", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogVO> queryAuditLogs(AuditLogQuery query) {
        PageRequest pageRequest = PageRequest.of(query.getPage(), query.getSize(), Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<AuditLog> logs;

        if (query.getUserId() != null) {
            logs = auditLogRepository.findByUserIdOrderByCreatedAtDesc(query.getUserId(), pageRequest);
        } else if (query.getOperation() != null) {
            AuditLog.OperationType opType = AuditLog.OperationType.valueOf(query.getOperation());
            logs = auditLogRepository.findByOperationOrderByCreatedAtDesc(opType, pageRequest);
        } else if (query.getStartTime() != null && query.getEndTime() != null) {
            LocalDateTime start = LocalDateTime.parse(query.getStartTime(), FORMATTER);
            LocalDateTime end = LocalDateTime.parse(query.getEndTime(), FORMATTER);
            logs = auditLogRepository.findByCreatedAtBetween(start, end, pageRequest);
        } else {
            logs = auditLogRepository.findAll(pageRequest);
        }

        return logs.map(this::toVO);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditStatistics getStatistics(Long userId, String startTime, String endTime) {
        LocalDateTime start = startTime != null ? LocalDateTime.parse(startTime, FORMATTER) : LocalDateTime.now().minusDays(30);
        LocalDateTime end = endTime != null ? LocalDateTime.parse(endTime, FORMATTER) : LocalDateTime.now();

        List<AuditLog> logs;
        if (userId != null) {
            logs = auditLogRepository.findByUserIdAndCreatedAtBetween(userId, start, end);
        } else {
            logs = auditLogRepository.findByCreatedAtBetween(start, end, PageRequest.of(0, 10000)).getContent();
        }

        long totalOps = logs.size();
        long successCount = logs.stream().filter(l -> l.getResult() == AuditLog.ResultType.SUCCESS).count();
        long failureCount = logs.stream().filter(l -> l.getResult() == AuditLog.ResultType.FAILURE).count();

        List<OperationCount> opCounts = auditLogRepository.countByOperationSince(start).stream()
                .map(row -> new OperationCount(row[0].toString(), (Long) row[1]))
                .collect(Collectors.toList());

        return new AuditStatistics(totalOps, successCount, failureCount, opCounts);
    }

    @Override
    @Transactional
    public long cleanExpiredLogs(int retentionDays) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        long deleted = auditLogRepository.deleteByCreatedAtBefore(cutoff);
        log.info("已清理 {} 条过期审计日志（保留天数: {}）", deleted, retentionDays);
        return deleted;
    }

    private AuditLogVO toVO(AuditLog entity) {
        return AuditLogVO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .username(entity.getUsername())
                .realName(entity.getRealName())
                .clientIp(entity.getClientIp())
                .operation(entity.getOperation().name())
                .resourceType(entity.getResourceType())
                .resourceId(entity.getResourceId())
                .resourceName(entity.getResourceName())
                .description(entity.getDescription())
                .requestMethod(entity.getRequestMethod())
                .requestUrl(entity.getRequestUrl())
                .responseStatus(entity.getResponseStatus())
                .result(entity.getResult().name())
                .duration(entity.getDuration())
                .dataLevel(entity.getDataLevel())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
