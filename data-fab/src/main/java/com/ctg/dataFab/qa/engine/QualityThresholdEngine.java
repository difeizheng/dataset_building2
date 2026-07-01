package com.ctg.dataFab.qa.engine;

import com.ctg.dataFab.common.enums.DataModality;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 质量阈值引擎
 * 实现 M2 四模态质量阈值检测
 *
 * 文本：完整率>99.5%、语法正确率>98%、重复率<1%、毒性<0.1%、领域CV<0.3
 * 图像：IoU>0.85、分类>95%、pHash重复<2%、违规<0.05%、最短边>512px
 * 音频：CER<3%、SNR>20dB、总时长>1000h、说话人均衡、涉密识别0漏放
 * 视频：分辨率>720p、帧率>24fps、时间偏差<0.5s、类别>100、涉黄/政0漏放
 *
 * @author Developer
 * @since 2026-07-01
 */
@Slf4j
public class QualityThresholdEngine {

    /**
     * 质量评估结果
     */
    @Data
    public static class QualityResult {
        private boolean passed;
        private Map<String, Object> metrics;
        private List<GateCheck> gates;

        public QualityResult() {
            this.gates = new ArrayList<>();
        }
    }

    /**
     * 门禁检查项
     */
    @Data
    public static class GateCheck {
        private String name;
        private double threshold;
        private Double actual; // null表示缺失
        private boolean passed;
        private String operator; // >, <, >=, <=
        private boolean missing; // 指标是否缺失

        public GateCheck(String name, double threshold, Double actual, String operator) {
            this.name = name;
            this.threshold = threshold;
            this.actual = actual;
            this.operator = operator;
            this.missing = (actual == null);

            // H2修复: 缺失指标判定为FAIL
            if (actual == null) {
                this.passed = false;
                return;
            }

            switch (operator) {
                case ">":
                    this.passed = actual > threshold;
                    break;
                case ">=":
                    this.passed = actual >= threshold;
                    break;
                case "<":
                    this.passed = actual < threshold;
                    break;
                case "<=":
                    this.passed = actual <= threshold;
                    break;
                default:
                    this.passed = false;
            }
        }
    }

    /**
     * 评估数据集质量
     *
     * @param modality 数据模态
     * @param metrics 实际指标
     * @return 评估结果
     */
    public static QualityResult evaluate(DataModality modality, Map<String, Object> metrics) {
        QualityResult result = new QualityResult();
        result.setMetrics(metrics);

        List<GateCheck> gates = new ArrayList<>();

        switch (modality) {
            case TEXT:
                gates.addAll(evaluateText(metrics));
                break;
            case IMAGE:
                gates.addAll(evaluateImage(metrics));
                break;
            case AUDIO:
                gates.addAll(evaluateAudio(metrics));
                break;
            case VIDEO:
                gates.addAll(evaluateVideo(metrics));
                break;
            default:
                throw new IllegalArgumentException("不支持的数据模态: " + modality);
        }

        result.setGates(gates);
        result.setPassed(gates.stream().allMatch(GateCheck::isPassed));

        log.info("质量评估完成: 模态={}, 通过={}, 门禁数={}",
                modality, result.isPassed(), gates.size());

        return result;
    }

    /**
     * 文本质量评估
     */
    private static List<GateCheck> evaluateText(Map<String, Object> metrics) {
        List<GateCheck> gates = new ArrayList<>();

        // 完整率 > 99.5%
        gates.add(new GateCheck("完整率", 99.5,
                getDoubleOrNull(metrics, "completeness_rate"), ">"));

        // 语法正确率 > 98%
        gates.add(new GateCheck("语法正确率", 98.0,
                getDoubleOrNull(metrics, "grammar_accuracy"), ">"));

        // 重复率 < 1%
        gates.add(new GateCheck("重复率", 1.0,
                getDoubleOrNull(metrics, "duplicate_rate"), "<"));

        // 毒性 < 0.1%
        gates.add(new GateCheck("毒性", 0.1,
                getDoubleOrNull(metrics, "toxicity_rate"), "<"));

        // 领域CV < 0.3
        gates.add(new GateCheck("领域CV", 0.3,
                getDoubleOrNull(metrics, "domain_cv"), "<"));

        return gates;
    }

    /**
     * 图像质量评估
     */
    private static List<GateCheck> evaluateImage(Map<String, Object> metrics) {
        List<GateCheck> gates = new ArrayList<>();

        // IoU > 0.85
        gates.add(new GateCheck("IoU", 0.85,
                getDoubleOrNull(metrics, "iou"), ">"));

        // 分类准确率 > 95%
        gates.add(new GateCheck("分类准确率", 95.0,
                getDoubleOrNull(metrics, "classification_accuracy"), ">"));

        // pHash重复 < 2%
        gates.add(new GateCheck("pHash重复", 2.0,
                getDoubleOrNull(metrics, "phash_duplicate"), "<"));

        // 违规 < 0.05%
        gates.add(new GateCheck("违规", 0.05,
                getDoubleOrNull(metrics, "violation_rate"), "<"));

        // 最短边 > 512px
        gates.add(new GateCheck("最短边", 512.0,
                getDoubleOrNull(metrics, "min_edge_length"), ">"));

        return gates;
    }

    /**
     * 音频质量评估
     */
    private static List<GateCheck> evaluateAudio(Map<String, Object> metrics) {
        List<GateCheck> gates = new ArrayList<>();

        // CER < 3%
        gates.add(new GateCheck("CER", 3.0,
                getDoubleOrNull(metrics, "cer"), "<"));

        // SNR > 20dB
        gates.add(new GateCheck("SNR", 20.0,
                getDoubleOrNull(metrics, "snr"), ">"));

        // 总时长 > 1000h
        gates.add(new GateCheck("总时长", 1000.0,
                getDoubleOrNull(metrics, "total_hours"), ">"));

        // 说话人均衡度 > 80%
        gates.add(new GateCheck("说话人均衡度", 80.0,
                getDoubleOrNull(metrics, "speaker_balance"), ">"));

        // 涉密识别 = 0
        gates.add(new GateCheck("涉密识别", 0.0,
                getDoubleOrNull(metrics, "secret_leak"), "<="));

        return gates;
    }

    /**
     * 视频质量评估
     */
    private static List<GateCheck> evaluateVideo(Map<String, Object> metrics) {
        List<GateCheck> gates = new ArrayList<>();

        // 分辨率 > 720p (高度)
        gates.add(new GateCheck("分辨率", 720.0,
                getDoubleOrNull(metrics, "resolution_height"), ">"));

        // 帧率 > 24fps
        gates.add(new GateCheck("帧率", 24.0,
                getDoubleOrNull(metrics, "frame_rate"), ">"));

        // 时间偏差 < 0.5s
        gates.add(new GateCheck("时间偏差", 0.5,
                getDoubleOrNull(metrics, "time_deviation"), "<"));

        // 类别数 > 100
        gates.add(new GateCheck("类别数", 100.0,
                getDoubleOrNull(metrics, "category_count"), ">"));

        // 涉黄/政 = 0
        gates.add(new GateCheck("涉黄/政", 0.0,
                getDoubleOrNull(metrics, "inappropriate_content"), "<="));

        return gates;
    }

    /**
     * 从Map中安全获取double值，缺失返回null
     */
    private static Double getDoubleOrNull(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
