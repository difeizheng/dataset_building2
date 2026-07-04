package com.ctg.kbFab.retrieval.controller;

import com.ctg.kbFab.common.dto.ApiResponse;
import com.ctg.kbFab.retrieval.dto.RetrievalRequest;
import com.ctg.kbFab.retrieval.dto.RetrievalResult;
import com.ctg.kbFab.retrieval.service.RetrievalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 检索控制器
 *
 * @author Developer
 * @since 2026-07-01
 */
@RestController
@RequestMapping("/api/v1/kb/retrieval")
@RequiredArgsConstructor
@Tag(name = "知识检索", description = "混合检索API（向量+图谱+全文+重排）")
public class RetrievalController {

    private final RetrievalService retrievalService;

    @PostMapping("/search")
    @Operation(summary = "执行检索")
    public ApiResponse<List<RetrievalResult>> retrieve(@Valid @RequestBody RetrievalRequest request) {
        List<RetrievalResult> results = retrievalService.retrieve(request);
        return ApiResponse.success(results);
    }

    @GetMapping("/simple")
    @Operation(summary = "简单检索")
    public ApiResponse<List<RetrievalResult>> simpleRetrieve(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") Integer topK) {
        List<RetrievalResult> results = retrievalService.simpleRetrieve(query, topK);
        return ApiResponse.success(results);
    }
}
