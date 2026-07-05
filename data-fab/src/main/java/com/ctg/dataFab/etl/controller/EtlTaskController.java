package com.ctg.dataFab.etl.controller;

import com.ctg.dataFab.access.annotation.AuditDataAccess;
import com.ctg.dataFab.common.dto.ApiResponse;
import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.common.dto.PageResponse;
import com.ctg.dataFab.etl.entity.EtlTask;
import com.ctg.dataFab.etl.service.EtlTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 清洗任务控制器
 *
 * @author Developer
 * @since 2026-07-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/etl/tasks")
@RequiredArgsConstructor
@Tag(name = "清洗任务管理", description = "清洗任务的创建和执行接口")
public class EtlTaskController {

    private final EtlTaskService etlTaskService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @AuditDataAccess(action = "WRITE", resourceType = "ETL_TASK")
    @Operation(summary = "创建清洗任务")
    public ApiResponse<Long> createTask(
            @RequestParam Long datasetId,
            @RequestParam String taskName) {
        Long id = etlTaskService.createTask(datasetId, taskName);
        return ApiResponse.success(id);
    }

    @PostMapping("/{id}/execute")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @AuditDataAccess(action = "WRITE", resourceType = "ETL_TASK")
    @Operation(summary = "执行清洗任务")
    public ApiResponse<Void> executeTask(@PathVariable Long id) {
        etlTaskService.executeTask(id);
        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'AUDITOR')")
    @AuditDataAccess(action = "READ", resourceType = "ETL_TASK")
    @Operation(summary = "获取清洗任务详情")
    public ApiResponse<EtlTask> getTask(@PathVariable Long id) {
        EtlTask task = etlTaskService.getTaskById(id);
        return ApiResponse.success(task);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'AUDITOR')")
    @Operation(summary = "分页查询清洗任务")
    public ApiResponse<PageResponse<EtlTask>> listTasks(
            @RequestParam(required = false) Long datasetId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(pageNum);
        pageRequest.setPageSize(pageSize);

        var page = etlTaskService.listTasks(datasetId, status, pageRequest);
        PageResponse<EtlTask> response = PageResponse.of(
                page.getRecords(),
                page.getTotal(),
                (int) page.getCurrent(),
                (int) page.getSize()
        );

        return ApiResponse.success(response);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @AuditDataAccess(action = "WRITE", resourceType = "ETL_TASK")
    @Operation(summary = "取消清洗任务")
    public ApiResponse<Void> cancelTask(@PathVariable Long id) {
        etlTaskService.cancelTask(id);
        return ApiResponse.success();
    }
}
