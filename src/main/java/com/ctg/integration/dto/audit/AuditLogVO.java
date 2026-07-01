package com.ctg.integration.dto.audit;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审计日志视图对象
 *
 * @author CTG
 * @since 2026-07-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogVO {

    private Long id;
    private Long userId;
    private String username;
    private String realName;
    private String clientIp;
    private String operation;
    private String resourceType;
    private String resourceId;
    private String resourceName;
    private String description;
    private String requestMethod;
    private String requestUrl;
    private Integer responseStatus;
    private String result;
    private Long duration;
    private String dataLevel;
    private LocalDateTime createdAt;
}
