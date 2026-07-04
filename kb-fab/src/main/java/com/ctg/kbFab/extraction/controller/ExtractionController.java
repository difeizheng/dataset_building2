package com.ctg.kbFab.extraction.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.kbFab.common.dto.ApiResponse;
import com.ctg.kbFab.extraction.dto.ExtractionRequest;
import com.ctg.kbFab.extraction.dto.ExtractionResult;
import com.ctg.kbFab.extraction.entity.ExtractionTask;
import com.ctg.kbFab.extraction.service.ExtractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 知识抽取控制器
 *
 * @author Developer
 * @since 2026-07-01
 */
@RestController
@RequestMapping("/api/v1/kb/extraction")
@RequiredArgsConstructor
@Tag(name = "知识抽取", description = "知识抽取引擎API")
public class ExtractionController {

    private final ExtractionService extractionService;

    @PostMapping("/tasks")
    @Operation(summary = "创建抽取任务")
    public ApiResponse<ExtractionTask> createTask(@Valid @RequestBody ExtractionRequest request) {
        ExtractionTask task = extractionService.createTask(request);
        return ApiResponse.success(task);
    }

    @PostMapping("/tasks/{taskId}/execute")
    @Operation(summary = "执行抽取任务")
    public ApiResponse<ExtractionResult> executeTask(@PathVariable String taskId) {
        ExtractionResult result = extractionService.executeTask(taskId);
        return ApiResponse.success(result);
    }

    @GetMapping("/tasks/{taskId}")
    @Operation(summary = "获取任务详情")
    public ApiResponse<ExtractionTask> getTask(@PathVariable String taskId) {
        ExtractionTask task = extractionService.getTask(taskId);
        return ApiResponse.success(task);
    }

    @GetMapping("/tasks")
    @Operation(summary = "分页查询任务")
    public ApiResponse<Page<ExtractionTask>> listTasks(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<ExtractionTask> tasks = extractionService.listTasks(page, size);
        return ApiResponse.success(tasks);
    }

    @DeleteMapping("/tasks/{taskId}")
    @Operation(summary = "删除任务")
    public ApiResponse<Void> deleteTask(@PathVariable String taskId) {
        extractionService.deleteTask(taskId);
        return ApiResponse.success();
    }
}
