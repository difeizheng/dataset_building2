package com.ctg.kbFab.quality.controller;

import com.ctg.kbFab.common.dto.ApiResponse;
import com.ctg.kbFab.quality.entity.QualityEvaluation;
import com.ctg.kbFab.quality.service.QualityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/kb/quality")
@RequiredArgsConstructor
@Tag(name = "知识质量", description = "知识质量评估API")
public class QualityController {

    private final QualityService qualityService;

    @PostMapping("/evaluate/{knowledgeId}")
    @Operation(summary = "执行质量评估")
    public ApiResponse<QualityEvaluation> evaluate(@PathVariable String knowledgeId) {
        QualityEvaluation evaluation = qualityService.evaluate(knowledgeId);
        return ApiResponse.success(evaluation);
    }

    @GetMapping("/latest/{knowledgeId}")
    @Operation(summary = "获取最新评估结果")
    public ApiResponse<QualityEvaluation> getLatest(@PathVariable String knowledgeId) {
        QualityEvaluation evaluation = qualityService.getLatestEvaluation(knowledgeId);
        return ApiResponse.success(evaluation);
    }
}
