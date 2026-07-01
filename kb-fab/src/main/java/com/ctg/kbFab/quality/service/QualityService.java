package com.ctg.kbFab.quality.service;

import com.ctg.kbFab.quality.engine.QualityAssessmentEngine;
import com.ctg.kbFab.quality.entity.QualityEvaluation;
import com.ctg.kbFab.storage.entity.KnowledgeEntry;
import com.ctg.kbFab.storage.mapper.KnowledgeEntryMapper;
import com.ctg.kbFab.common.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class QualityService {

    private final QualityAssessmentEngine qualityEngine;
    private final QualityEvaluationMapper qualityMapper;
    private final KnowledgeEntryMapper knowledgeMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public QualityEvaluation evaluate(String knowledgeId) {
        KnowledgeEntry entry = knowledgeMapper.selectById(knowledgeId);
        if (entry == null) {
            throw new BusinessException("Knowledge entry not found: " + knowledgeId);
        }

        QualityAssessmentEngine.QualityResult result = qualityEngine.assess(entry);

        QualityEvaluation evaluation = new QualityEvaluation();
        evaluation.setKnowledgeId(knowledgeId);
        evaluation.setCompletenessScore(result.getCompletenessScore());
        evaluation.setConsistencyScore(result.getConsistencyScore());
        evaluation.setAccuracyScore(result.getAccuracyScore());
        evaluation.setOverallScore(result.getOverallScore());
        evaluation.setPassed(result.isPassed());
        evaluation.setEvaluator("system");
        evaluation.setCreateTime(LocalDateTime.now());
        evaluation.setDeleted(0);

        try {
            evaluation.setDetailJson(objectMapper.writeValueAsString(result));
        } catch (Exception e) {
            log.warn("Failed to serialize quality detail", e);
        }

        qualityMapper.insert(evaluation);
        log.info("Quality evaluation for {}: score={}, passed={}", knowledgeId, result.getOverallScore(), result.isPassed());
        return evaluation;
    }

    public QualityEvaluation getLatestEvaluation(String knowledgeId) {
        return qualityMapper.selectList(null).stream()
                .filter(e -> knowledgeId.equals(e.getKnowledgeId()))
                .reduce((a, b) -> b)
                .orElse(null);
    }
}
