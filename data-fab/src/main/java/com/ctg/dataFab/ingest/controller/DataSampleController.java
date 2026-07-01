package com.ctg.dataFab.ingest.controller;

import com.ctg.dataFab.common.dto.ApiResponse;
import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.common.dto.PageResponse;
import com.ctg.dataFab.ingest.dto.DataSampleCreateRequest;
import com.ctg.dataFab.ingest.entity.DataSample;
import com.ctg.dataFab.ingest.service.DataSampleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 数据样本控制器
 *
 * @author Developer
 * @since 2026-07-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/data/samples")
@RequiredArgsConstructor
@Tag(name = "数据样本管理", description = "数据样本的增删改查接口")
public class DataSampleController {

    private final DataSampleService dataSampleService;

    @PostMapping
    @Operation(summary = "创建数据样本")
    public ApiResponse<Long> createDataSample(@Valid @RequestBody DataSampleCreateRequest request) {
        Long id = dataSampleService.createDataSample(request);
        return ApiResponse.success(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取数据样本详情")
    public ApiResponse<DataSample> getDataSample(@PathVariable Long id) {
        DataSample dataSample = dataSampleService.getDataSampleById(id);
        return ApiResponse.success(dataSample);
    }

    @GetMapping
    @Operation(summary = "分页查询数据样本")
    public ApiResponse<PageResponse<DataSample>> listDataSamples(
            @RequestParam(required = false) Long datasetId,
            @RequestParam(required = false) Integer modality,
            @RequestParam(required = false) Integer dataLevel,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(pageNum);
        pageRequest.setPageSize(pageSize);

        var page = dataSampleService.listDataSamples(datasetId, modality, dataLevel, status, pageRequest);
        PageResponse<DataSample> response = PageResponse.of(
                page.getRecords(),
                page.getTotal(),
                (int) page.getCurrent(),
                (int) page.getSize()
        );

        return ApiResponse.success(response);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新数据样本状态")
    public ApiResponse<Void> updateDataSampleStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        dataSampleService.updateDataSampleStatus(id, status);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除数据样本")
    public ApiResponse<Void> deleteDataSample(@PathVariable Long id) {
        dataSampleService.deleteDataSample(id);
        return ApiResponse.success();
    }
}
