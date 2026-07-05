package com.ctg.dataFab.access.aspect;

import com.ctg.dataFab.access.annotation.AuditDataAccess;
import com.ctg.dataFab.access.service.AuditLogService;
import com.ctg.dataFab.common.enums.DataLevel;
import com.ctg.dataFab.common.security.JwtTokenProvider;
import com.ctg.dataFab.ingest.entity.DataSample;
import com.ctg.dataFab.ingest.entity.Dataset;
import com.ctg.dataFab.ingest.mapper.DataSampleMapper;
import com.ctg.dataFab.ingest.mapper.DatasetMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 数据访问审计切面
 * 拦截所有标注了 @AuditDataAccess 的方法，记录审计日志
 *
 * @author Developer
 * @since 2026-07-05
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DataAccessAuditAspect {

    private final AuditLogService auditLogService;
    private final DataSampleMapper dataSampleMapper;
    private final DatasetMapper datasetMapper;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 拦截数据访问方法
     */
    @Before("@annotation(auditDataAccess)")
    public void auditDataAccess(JoinPoint joinPoint, AuditDataAccess auditDataAccess) {
        try {
            // 获取当前用户信息
            String username = getCurrentUsername();
            Long userId = getCurrentUserId();

            // 获取请求信息
            HttpServletRequest request = getCurrentRequest();
            String ipAddress = request != null ? getClientIpAddress(request) : "unknown";
            String httpMethod = request != null ? request.getMethod() : "unknown";
            String requestPath = request != null ? request.getRequestURI() : "unknown";
            String userAgent = request != null ? request.getHeader("User-Agent") : "unknown";

            // 解析资源ID和数据分级
            Long resourceId = extractResourceId(joinPoint);
            String dataLevel = resolveDataLevel(auditDataAccess, resourceId, auditDataAccess.resourceType());

            // 记录审计日志
            auditLogService.log(
                    userId,
                    username,
                    auditDataAccess.action(),
                    auditDataAccess.resourceType(),
                    resourceId,
                    dataLevel,
                    "SUCCESS",
                    "数据访问操作",
                    ipAddress,
                    httpMethod,
                    requestPath,
                    userAgent
            );

        } catch (Exception e) {
            // 审计失败不应影响业务
            log.error("审计日志记录失败", e);
        }
    }

    /**
     * 获取当前用户名
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        return "anonymous";
    }

    /**
     * 获取当前用户ID（从JWT中解析）
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof String token) {
            try {
                return jwtTokenProvider.getUserIdFromToken(token);
            } catch (Exception e) {
                log.debug("从JWT提取userId失败: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 获取当前HTTP请求
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 从方法参数中提取资源ID
     */
    private Long extractResourceId(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            String paramName = parameters[i].getName();
            if (paramName.equals("id") || paramName.equals("sampleId") ||
                paramName.equals("datasetId") || paramName.equals("recordId")) {
                if (args[i] instanceof Long) {
                    return (Long) args[i];
                } else if (args[i] instanceof Integer) {
                    return ((Integer) args[i]).longValue();
                }
            }
        }

        return null;
    }

    /**
     * 解析数据分级
     */
    private String resolveDataLevel(AuditDataAccess auditDataAccess, Long resourceId, String resourceType) {
        if (!auditDataAccess.dataLevel().isEmpty()) {
            return auditDataAccess.dataLevel();
        }

        if (resourceId == null) {
            return "UNKNOWN";
        }

        try {
            if ("DATA_SAMPLE".equals(resourceType)) {
                DataSample sample = dataSampleMapper.selectById(resourceId);
                if (sample != null && sample.getDataLevel() != null) {
                    return DataLevel.fromCode(sample.getDataLevel()).name();
                }
            } else if ("DATASET".equals(resourceType)) {
                Dataset dataset = datasetMapper.selectById(resourceId);
                if (dataset != null && dataset.getDataLevel() != null) {
                    return DataLevel.fromCode(dataset.getDataLevel()).name();
                }
            }
        } catch (Exception e) {
            log.warn("解析数据分级失败: resourceId={}, resourceType={}", resourceId, resourceType, e);
        }

        return "UNKNOWN";
    }
}
