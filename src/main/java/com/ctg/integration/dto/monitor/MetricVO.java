package com.ctg.integration.dto.monitor;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 指标视图对象
 *
 * @author CTG
 * @since 2026-07-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricVO {

    private String name;
    private String description;
    private Double value;
    private String unit;
    private String type;
    private Map<String, String> tags;
    private LocalDateTime timestamp;
}
