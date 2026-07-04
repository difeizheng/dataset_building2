package com.ctg.kbFab.quality;

import com.ctg.kbFab.common.enums.KnowledgeStatus;
import com.ctg.kbFab.common.enums.KnowledgeType;
import com.ctg.kbFab.quality.engine.QualityAssessmentEngine;
import com.ctg.kbFab.storage.entity.KnowledgeEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 质量评估引擎测试
 */
class QualityAssessmentEngineTest {

    private QualityAssessmentEngine engine;

    @BeforeEach
    void setUp() {
        engine = new QualityAssessmentEngine();
    }

    @Test
    void testAssessCompleteness_fullEntry() {
        KnowledgeEntry entry = createFullEntry();
        double score = engine.assessCompleteness(entry);
        assertTrue(score >= 0.8);
    }

    @Test
    void testAssessCompleteness_emptyEntry() {
        KnowledgeEntry entry = new KnowledgeEntry();
        double score = engine.assessCompleteness(entry);
        assertTrue(score < 0.3);
    }

    @Test
    void testAssessCompleteness_partialEntry() {
        KnowledgeEntry entry = new KnowledgeEntry();
        entry.setTitle("测试标题");
        entry.setContent("测试内容");
        double score = engine.assessCompleteness(entry);
        assertTrue(score >= 0.4 && score <= 0.8);
    }

    @Test
    void testAssessConsistency_goodEntry() {
        KnowledgeEntry entry = createFullEntry();
        double score = engine.assessConsistency(entry);
        assertTrue(score >= 0.7);
    }

    @Test
    void testAssessConsistency_withConflicts() {
        KnowledgeEntry entry = new KnowledgeEntry();
        entry.setTitle("测试");
        entry.setContent("这里存在矛盾和冲突的内容，待确认");
        double score = engine.assessConsistency(entry);
        assertTrue(score < 0.8);
    }

    @Test
    void testAssessAccuracy_withSource() {
        KnowledgeEntry entry = createFullEntry();
        double score = engine.assessAccuracy(entry);
        assertTrue(score >= 0.8);
    }

    @Test
    void testAssessAccuracy_withoutSource() {
        KnowledgeEntry entry = new KnowledgeEntry();
        entry.setTitle("测试");
        double score = engine.assessAccuracy(entry);
        assertTrue(score < 0.9);
    }

    @Test
    void testAssess_overall() {
        KnowledgeEntry entry = createFullEntry();
        QualityAssessmentEngine.QualityResult result = engine.assess(entry);
        assertNotNull(result);
        assertTrue(result.getOverallScore() > 0);
        assertTrue(result.getCompletenessScore() > 0);
        assertTrue(result.getConsistencyScore() > 0);
        assertTrue(result.getAccuracyScore() > 0);
    }

    private KnowledgeEntry createFullEntry() {
        KnowledgeEntry entry = new KnowledgeEntry();
        entry.setTitle("三峡集团人工智能知识库");
        entry.setContent("这是一段关于三峡集团人工智能知识库建设的详细内容，包含了知识抽取、存储、检索和问答等多个模块的技术方案。");
        entry.setKnowledgeType(KnowledgeType.ENTITY);
        entry.setStatus(KnowledgeStatus.PUBLISHED);
        entry.setDomain("engineering");
        entry.setSourceDocId("doc-001");
        entry.setVersion(2);
        return entry;
    }
}
