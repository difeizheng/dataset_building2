package com.ctg.integration.dto.monitor;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 健康状态视图对象
 *
 * @author CTG
 * @since 2026-07-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthStatusVO {

    private String status;
    private String version;
    private LocalDateTime checkTime;
    private Map<String, ComponentHealth> components;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComponentHealth {
        private String name;
        private String status;
        private String message;
        private Long responseTime;
    }
}
