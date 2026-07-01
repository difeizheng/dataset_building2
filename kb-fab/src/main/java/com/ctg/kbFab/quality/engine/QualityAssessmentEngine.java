package com.ctg.kbFab.quality.engine;

import com.ctg.kbFab.storage.entity.KnowledgeEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 知识质量评估引擎
 * 评估维度：完整性、一致性、准确性
 *
 * @author Developer
 * @since 2026-07-01
 */
@Slf4j
@Component
public class QualityAssessmentEngine {

    @Value("${kb-fab.quality.completeness-threshold:0.85}")
    private double completenessThreshold;

    @Value("${kb-fab.quality.consistency-threshold:0.80}")
    private double consistencyThreshold;

    @Value("${kb-fab.quality.accuracy-threshold:0.90}")
    private double accuracyThreshold;

    /**
     * 评估完整性
     */
    public double assessCompleteness(KnowledgeEntry entry) {
        double score = 0.0;
        int checks = 0;

        // 标题检查
        if (entry.getTitle() != null && !entry.getTitle().isBlank()) {
            score += 0.2;
        }
        checks++;

        // 内容检查
        if (entry.getContent() != null && !entry.getContent().isBlank()) {
            score += 0.3;
            // 内容长度加分
            if (entry.getContent().length() > 50) {
                score += 0.1;
            }
        }
        checks++;

        // 类型检查
        if (entry.getKnowledgeType() != null) {
            score += 0.15;
        }
        checks++;

        // 领域检查
        if (entry.getDomain() != null && !entry.getDomain().isBlank()) {
            score += 0.15;
        }
        checks++;

        // 来源检查
        if (entry.getSourceDocId() != null) {
            score += 0.1;
        }
        checks++;

        return Math.min(1.0, score);
    }

    /**
     * 评估一致性
     */
    public double assessConsistency(KnowledgeEntry entry) {
        double score = 1.0;

        // 检查内容中是否有矛盾标记
        if (entry.getContent() != null) {
            String content = entry.getContent();
            if (content.contains("待确认") || content.contains("TBD")) {
                score -= 0.3;
            }
            if (content.contains("矛盾") || content.contains("冲突")) {
                score -= 0.2;
            }
        }

        // 检查标题和内容是否匹配（简单长度比）
        if (entry.getTitle() != null && entry.getContent() != null) {
            if (entry.getContent().length() < entry.getTitle().length()) {
                score -= 0.2;
            }
        }

        return Math.max(0.0, Math.min(1.0, score));
    }

    /**
     * 评估准确性
     */
    public double assessAccuracy(KnowledgeEntry entry) {
        double score = 0.7; // 基础分

        // 有来源文档加分
        if (entry.getSourceDocId() != null) {
            score += 0.15;
        }

        // 有版本号且大于1加分（经过迭代）
        if (entry.getVersion() != null && entry.getVersion() > 1) {
            score += 0.1;
        }

        // 已发布状态加分
        if (entry.getStatus() != null && "published".equals(entry.getStatus().getCode())) {
            score += 0.05;
        }

        return Math.min(1.0, score);
    }

    /**
     * 综合评估
     */
    public QualityResult assess(KnowledgeEntry entry) {
        double completeness = assessCompleteness(entry);
        double consistency = assessConsistency(entry);
        double accuracy = assessAccuracy(entry);
        double overall = completeness * 0.4 + consistency * 0.3 + accuracy * 0.3;

        boolean passed = completeness >= completenessThreshold
                && consistency >= consistencyThreshold
                && accuracy >= accuracyThreshold;

        QualityResult result = new QualityResult();
        result.setCompletenessScore(completeness);
        result.setConsistencyScore(consistency);
        result.setAccuracyScore(accuracy);
        result.setOverallScore(overall);
        result.setPassed(passed);

        log.debug("Quality assessment for {}: completeness={}, consistency={}, accuracy={}, overall={}, passed={}",
                entry.getId(), completeness, consistency, accuracy, overall, passed);

        return result;
    }

    public static class QualityResult {
        private double completenessScore;
        private double consistencyScore;
        private double accuracyScore;
        private double overallScore;
        private boolean passed;

        public double getCompletenessScore() { return completenessScore; }
        public void setCompletenessScore(double v) { this.completenessScore = v; }
        public double getConsistencyScore() { return consistencyScore; }
        public void setConsistencyScore(double v) { this.consistencyScore = v; }
        public double getAccuracyScore() { return accuracyScore; }
        public void setAccuracyScore(double v) { this.accuracyScore = v; }
        public double getOverallScore() { return overallScore; }
        public void setOverallScore(double v) { this.overallScore = v; }
        public boolean isPassed() { return passed; }
        public void setPassed(boolean v) { this.passed = v; }
    }
}
