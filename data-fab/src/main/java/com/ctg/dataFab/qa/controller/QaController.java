package com.ctg.dataFab.qa.controller;

import com.ctg.dataFab.common.dto.ApiResponse;
import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.common.dto.PageResponse;
import com.ctg.dataFab.qa.dto.EvaluateRequest;
import com.ctg.dataFab.qa.dto.EvaluateResponse;
import com.ctg.dataFab.qa.entity.QaTask;
import com.ctg.dataFab.qa.service.QaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 质量评估控制器
 * 对应接口：POST /api/v1/qa/evaluate
 *
 * @author Developer
 * @since 2026-07-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/qa")
@RequiredArgsConstructor
@Tag(name = "质量管控中心", description = "质量评估、三审三校流程接口")
public class QaController {

    private final QaService qaService;

    @PostMapping("/evaluate")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "执行质量评估")
    public ApiResponse<EvaluateResponse> evaluate(@Valid @RequestBody EvaluateRequest request) {
        EvaluateResponse response = qaService.evaluate(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/tasks/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'AUDITOR')")
    @Operation(summary = "获取评估任务详情")
    public ApiResponse<QaTask> getTask(@PathVariable Long id) {
        QaTask task = qaService.getTaskById(id);
        return ApiResponse.success(task);
    }

    @GetMapping("/tasks")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'AUDITOR')")
    @Operation(summary = "分页查询评估任务")
    public ApiResponse<PageResponse<QaTask>> listTasks(
            @RequestParam(required = false) Long datasetId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(pageNum);
        pageRequest.setPageSize(pageSize);

        var page = qaService.listTasks(datasetId, status, pageRequest);
        PageResponse<QaTask> response = PageResponse.of(
                page.getRecords(),
                page.getTotal(),
                (int) page.getCurrent(),
                (int) page.getSize()
        );

        return ApiResponse.success(response);
    }

    @PostMapping("/tasks/{id}/manual-review")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @Operation(summary = "人工复审")
    public ApiResponse<Void> manualReview(
            @PathVariable Long id,
            @RequestParam boolean passed,
            @RequestParam String comment,
            @RequestParam String reviewer) {
        qaService.manualReview(id, passed, comment, reviewer);
        return ApiResponse.success();
    }

    @PostMapping("/tasks/{id}/expert-review")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "专家终审")
    public ApiResponse<Void> expertReview(
            @PathVariable Long id,
            @RequestParam boolean passed,
            @RequestParam String comment,
            @RequestParam String reviewer) {
        qaService.expertReview(id, passed, comment, reviewer);
        return ApiResponse.success();
    }

    @PostMapping("/tasks/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "发布数据集")
    public ApiResponse<Void> publishDataset(@PathVariable Long id) {
        qaService.publishDataset(id);
        return ApiResponse.success();
    }
}
