package com.ctg.integration.dto.integration;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统状态视图对象
 *
 * @author CTG
 * @since 2026-07-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemStatusVO {

    private String systemName;
    private String systemCode;
    private boolean available;
    private String status;
    private Long responseTime;
    private LocalDateTime lastCheckTime;
    private String errorMessage;
}
