package com.ctg.dataFab.qa;

import com.ctg.dataFab.common.enums.DataModality;
import com.ctg.dataFab.qa.engine.QualityThresholdEngine;
import com.ctg.dataFab.qa.engine.QualityThresholdEngine.QualityResult;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 质量阈值引擎单元测试
 * 覆盖 M2 四模态质量阈值检测
 *
 * @author Developer
 * @since 2026-07-01
 */
class QualityThresholdEngineTest {

    @Test
    void testText_allPass() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("completeness_rate", 99.8);  // > 99.5%
        metrics.put("grammar_accuracy", 99.0);     // > 98%
        metrics.put("duplicate_rate", 0.5);        // < 1%
        metrics.put("toxicity_rate", 0.05);        // < 0.1%
        metrics.put("domain_cv", 0.2);             // < 0.3

        QualityResult result = QualityThresholdEngine.evaluate(DataModality.TEXT, metrics);
        assertTrue(result.isPassed(), "全部达标应通过");
        assertEquals(5, result.getGates().size(), "文本应有5个门禁");
    }

    @Test
    void testText_completenessFail() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("completeness_rate", 99.0);    // < 99.5% FAIL
        metrics.put("grammar_accuracy", 99.0);
        metrics.put("duplicate_rate", 0.5);
        metrics.put("toxicity_rate", 0.05);
        metrics.put("domain_cv", 0.2);

        QualityResult result = QualityThresholdEngine.evaluate(DataModality.TEXT, metrics);
        assertFalse(result.isPassed(), "完整率不达标应失败");
    }

    @Test
    void testText_toxicityFail() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("completeness_rate", 99.8);
        metrics.put("grammar_accuracy", 99.0);
        metrics.put("duplicate_rate", 0.5);
        metrics.put("toxicity_rate", 0.2);         // > 0.1% FAIL
        metrics.put("domain_cv", 0.2);

        QualityResult result = QualityThresholdEngine.evaluate(DataModality.TEXT, metrics);
        assertFalse(result.isPassed(), "毒性不达标应失败");
    }

    @Test
    void testImage_allPass() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("iou", 0.90);                  // > 0.85
        metrics.put("classification_accuracy", 97.0); // > 95%
        metrics.put("phash_duplicate", 1.0);       // < 2%
        metrics.put("violation_rate", 0.01);       // < 0.05%
        metrics.put("min_edge_length", 600.0);     // > 512px

        QualityResult result = QualityThresholdEngine.evaluate(DataModality.IMAGE, metrics);
        assertTrue(result.isPassed(), "图像全部达标应通过");
        assertEquals(5, result.getGates().size());
    }

    @Test
    void testImage_iouFail() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("iou", 0.80);                  // < 0.85 FAIL
        metrics.put("classification_accuracy", 97.0);
        metrics.put("phash_duplicate", 1.0);
        metrics.put("violation_rate", 0.01);
        metrics.put("min_edge_length", 600.0);

        QualityResult result = QualityThresholdEngine.evaluate(DataModality.IMAGE, metrics);
        assertFalse(result.isPassed(), "IoU不达标应失败");
    }

    @Test
    void testAudio_allPass() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("cer", 2.0);                   // < 3%
        metrics.put("snr", 25.0);                  // > 20dB
        metrics.put("total_hours", 1500.0);        // > 1000h
        metrics.put("speaker_balance", 85.0);      // > 80%
        metrics.put("secret_leak", 0.0);           // = 0

        QualityResult result = QualityThresholdEngine.evaluate(DataModality.AUDIO, metrics);
        assertTrue(result.isPassed(), "音频全部达标应通过");
        assertEquals(5, result.getGates().size());
    }

    @Test
    void testAudio_snrFail() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("cer", 2.0);
        metrics.put("snr", 15.0);                  // < 20dB FAIL
        metrics.put("total_hours", 1500.0);
        metrics.put("speaker_balance", 85.0);
        metrics.put("secret_leak", 0.0);

        QualityResult result = QualityThresholdEngine.evaluate(DataModality.AUDIO, metrics);
        assertFalse(result.isPassed(), "SNR不达标应失败");
    }

    @Test
    void testVideo_allPass() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("resolution_height", 1080.0);  // > 720p
        metrics.put("frame_rate", 30.0);           // > 24fps
        metrics.put("time_deviation", 0.3);        // < 0.5s
        metrics.put("category_count", 150.0);      // > 100
        metrics.put("inappropriate_content", 0.0); // = 0

        QualityResult result = QualityThresholdEngine.evaluate(DataModality.VIDEO, metrics);
        assertTrue(result.isPassed(), "视频全部达标应通过");
        assertEquals(5, result.getGates().size());
    }

    @Test
    void testVideo_resolutionFail() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("resolution_height", 480.0);   // < 720p FAIL
        metrics.put("frame_rate", 30.0);
        metrics.put("time_deviation", 0.3);
        metrics.put("category_count", 150.0);
        metrics.put("inappropriate_content", 0.0);

        QualityResult result = QualityThresholdEngine.evaluate(DataModality.VIDEO, metrics);
        assertFalse(result.isPassed(), "分辨率不达标应失败");
    }

    @Test
    void testMissingMetrics_defaultToZero() {
        Map<String, Object> metrics = new HashMap<>();
        // 空指标，所有值默认为0

        QualityResult result = QualityThresholdEngine.evaluate(DataModality.TEXT, metrics);
        assertFalse(result.isPassed(), "空指标应不通过");
    }
}
