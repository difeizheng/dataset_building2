package com.ctg.dataFab.ingest.controller;

import com.ctg.dataFab.access.annotation.AuditDataAccess;
import com.ctg.dataFab.common.dto.ApiResponse;
import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.common.dto.PageResponse;
import com.ctg.dataFab.ingest.dto.DatasetCreateRequest;
import com.ctg.dataFab.ingest.entity.Dataset;
import com.ctg.dataFab.ingest.service.DatasetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 数据集控制器
 *
 * @author Developer
 * @since 2026-07-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/data/datasets")
@RequiredArgsConstructor
@Tag(name = "数据集管理", description = "数据集的增删改查接口")
public class DatasetController {

    private final DatasetService datasetService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @AuditDataAccess(action = "WRITE", resourceType = "DATASET")
    @Operation(summary = "创建数据集")
    public ApiResponse<Long> createDataset(@Valid @RequestBody DatasetCreateRequest request) {
        Long id = datasetService.createDataset(request);
        return ApiResponse.success(id);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'AUDITOR')")
    @AuditDataAccess(action = "READ", resourceType = "DATASET")
    @Operation(summary = "获取数据集详情")
    public ApiResponse<Dataset> getDataset(@PathVariable Long id) {
        Dataset dataset = datasetService.getDatasetById(id);
        return ApiResponse.success(dataset);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'AUDITOR')")
    @Operation(summary = "分页查询数据集")
    public ApiResponse<PageResponse<Dataset>> listDatasets(
            @RequestParam(required = false) Integer modality,
            @RequestParam(required = false) Integer dataLevel,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(pageNum);
        pageRequest.setPageSize(pageSize);

        var page = datasetService.listDatasets(modality, dataLevel, status, pageRequest);
        PageResponse<Dataset> response = PageResponse.of(
                page.getRecords(),
                page.getTotal(),
                (int) page.getCurrent(),
                (int) page.getSize()
        );

        return ApiResponse.success(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @AuditDataAccess(action = "DELETE", resourceType = "DATASET")
    @Operation(summary = "删除数据集")
    public ApiResponse<Void> deleteDataset(@PathVariable Long id) {
        datasetService.deleteDataset(id);
        return ApiResponse.success();
    }
}
