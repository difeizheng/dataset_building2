package com.ctg.dataFab.qa.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 质量评估响应
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
public class EvaluateResponse {

    private Long taskId;
    private boolean passed;
    private Map<String, Object> metrics;
    private List<GateInfo> gates;

    @Data
    public static class GateInfo {
        private String name;
        private double threshold;
        private Double actual;
        private boolean passed;
        private String operator;
        private boolean missing;
    }
}
