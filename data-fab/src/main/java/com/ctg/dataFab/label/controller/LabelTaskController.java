package com.ctg.dataFab.label.controller;

import com.ctg.dataFab.common.dto.ApiResponse;
import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.common.dto.PageResponse;
import com.ctg.dataFab.label.dto.CreateLabelTaskRequest;
import com.ctg.dataFab.label.dto.SubmitLabelRequest;
import com.ctg.dataFab.label.entity.LabelTask;
import com.ctg.dataFab.label.service.LabelTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 标注任务控制器
 * 对应接口：POST /api/v1/label/tasks, POST /api/v1/label/submit, POST /api/v1/label/arbitrate
 *
 * @author Developer
 * @since 2026-07-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/label")
@RequiredArgsConstructor
@Tag(name = "标注工作台", description = "标注任务管理、标注提交、仲裁接口")
public class LabelTaskController {

    private final LabelTaskService labelTaskService;

    @PostMapping("/tasks")
    @Operation(summary = "创建标注任务")
    public ApiResponse<Long> createTask(@Valid @RequestBody CreateLabelTaskRequest request) {
        Long id = labelTaskService.createTask(request);
        return ApiResponse.success(id);
    }

    @GetMapping("/tasks/{id}")
    @Operation(summary = "获取标注任务详情")
    public ApiResponse<LabelTask> getTask(@PathVariable Long id) {
        LabelTask task = labelTaskService.getTaskById(id);
        return ApiResponse.success(task);
    }

    @GetMapping("/tasks")
    @Operation(summary = "分页查询标注任务")
    public ApiResponse<PageResponse<LabelTask>> listTasks(
            @RequestParam(required = false) Long datasetId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(pageNum);
        pageRequest.setPageSize(pageSize);

        var page = labelTaskService.listTasks(datasetId, status, pageRequest);
        PageResponse<LabelTask> response = PageResponse.of(
                page.getRecords(),
                page.getTotal(),
                (int) page.getCurrent(),
                (int) page.getSize()
        );

        return ApiResponse.success(response);
    }

    @PostMapping("/submit")
    @Operation(summary = "提交标注")
    public ApiResponse<Void> submitLabel(@Valid @RequestBody SubmitLabelRequest request) {
        labelTaskService.submitLabel(request);
        return ApiResponse.success();
    }

    @PostMapping("/arbitrate")
    @Operation(summary = "触发仲裁")
    public ApiResponse<Void> triggerArbitration(@RequestParam Long taskId) {
        labelTaskService.triggerArbitration(taskId);
        return ApiResponse.success();
    }

    @GetMapping("/tasks/{id}/iaa")
    @Operation(summary = "计算IAA得分")
    public ApiResponse<Double> calculateIaa(@PathVariable Long id) {
        Double kappa = labelTaskService.calculateIaaScore(id);
        return ApiResponse.success(kappa);
    }

    @PostMapping("/tasks/{id}/relabel")
    @Operation(summary = "触发重标")
    public ApiResponse<Void> triggerRelabel(@PathVariable Long id) {
        labelTaskService.triggerRelabel(id);
        return ApiResponse.success();
    }
}
