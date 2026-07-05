package com.ctg.dataFab.access.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.dataFab.access.entity.AuditLog;
import com.ctg.dataFab.access.mapper.AuditLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 审计日志服务
 * 提供审计日志的记录和查询功能
 *
 * @author Developer
 * @since 2026-07-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogMapper auditLogMapper;

    /**
     * 记录审计日志
     *
     * @param auditLog 审计日志实体
     */
    public void log(AuditLog auditLog) {
        if (auditLog.getCreateTime() == null) {
            auditLog.setCreateTime(LocalDateTime.now());
        }
        auditLogMapper.insert(auditLog);
        log.debug("审计日志已记录: action={}, resourceType={}, resourceId={}, userId={}, result={}",
                auditLog.getAction(), auditLog.getResourceType(), auditLog.getResourceId(),
                auditLog.getUserId(), auditLog.getResult());
    }

    /**
     * 简化记录方法
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param action 操作类型
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     * @param dataLevel 数据分级
     * @param result 结果
     * @param reason 原因
     * @param ipAddress IP地址
     * @param httpMethod HTTP方法
     * @param requestPath 请求路径
     * @param userAgent 用户代理
     */
    public void log(Long userId, String username, String action, String resourceType,
                    Long resourceId, String dataLevel, String result, String reason,
                    String ipAddress, String httpMethod, String requestPath, String userAgent) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(userId);
        auditLog.setUsername(username);
        auditLog.setAction(action);
        auditLog.setResourceType(resourceType);
        auditLog.setResourceId(resourceId);
        auditLog.setDataLevel(dataLevel);
        auditLog.setResult(result);
        auditLog.setReason(reason);
        auditLog.setIpAddress(ipAddress);
        auditLog.setHttpMethod(httpMethod);
        auditLog.setRequestPath(requestPath);
        auditLog.setUserAgent(userAgent);
        log(auditLog);
    }

    /**
     * 查询审计日志（分页）
     *
     * @param userId 用户ID（可选）
     * @param action 操作类型（可选）
     * @param resourceType 资源类型（可选）
     * @param dataLevel 数据分级（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    public IPage<AuditLog> queryLogs(Long userId, String action, String resourceType,
                                     String dataLevel, LocalDateTime startTime, LocalDateTime endTime,
                                     int pageNum, int pageSize) {
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();

        if (userId != null) {
            wrapper.eq(AuditLog::getUserId, userId);
        }
        if (action != null && !action.isEmpty()) {
            wrapper.eq(AuditLog::getAction, action);
        }
        if (resourceType != null && !resourceType.isEmpty()) {
            wrapper.eq(AuditLog::getResourceType, resourceType);
        }
        if (dataLevel != null && !dataLevel.isEmpty()) {
            wrapper.eq(AuditLog::getDataLevel, dataLevel);
        }
        if (startTime != null) {
            wrapper.ge(AuditLog::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(AuditLog::getCreateTime, endTime);
        }

        wrapper.orderByDesc(AuditLog::getCreateTime);

        return auditLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 根据ID查询审计日志
     *
     * @param id 日志ID
     * @return 审计日志
     */
    public AuditLog getById(Long id) {
        return auditLogMapper.selectById(id);
    }
}
