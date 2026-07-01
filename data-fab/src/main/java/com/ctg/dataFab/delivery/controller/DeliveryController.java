package com.ctg.dataFab.delivery.controller;

import com.ctg.dataFab.common.dto.ApiResponse;
import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.common.dto.PageResponse;
import com.ctg.dataFab.delivery.dto.PublishRequest;
import com.ctg.dataFab.delivery.dto.PublishResponse;
import com.ctg.dataFab.delivery.entity.DeliveryRecord;
import com.ctg.dataFab.delivery.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 数据集交付控制器
 * 对应接口：POST /api/v1/delivery/publish, GET /api/v1/delivery/{id}/download
 *
 * @author Developer
 * @since 2026-07-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/delivery")
@RequiredArgsConstructor
@Tag(name = "数据集交付", description = "数据集发布、下载、版本管理接口")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping("/publish")
    @Operation(summary = "发布数据集")
    public ApiResponse<PublishResponse> publish(@Valid @RequestBody PublishRequest request) {
        PublishResponse response = deliveryService.publish(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/records/{id}")
    @Operation(summary = "获取交付记录详情")
    public ApiResponse<DeliveryRecord> getRecord(@PathVariable Long id) {
        DeliveryRecord record = deliveryService.getRecordById(id);
        return ApiResponse.success(record);
    }

    @GetMapping("/records")
    @Operation(summary = "分页查询交付记录")
    public ApiResponse<PageResponse<DeliveryRecord>> listRecords(
            @RequestParam(required = false) Long datasetId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(pageNum);
        pageRequest.setPageSize(pageSize);

        var page = deliveryService.listRecords(datasetId, status, pageRequest);
        PageResponse<DeliveryRecord> response = PageResponse.of(
                page.getRecords(),
                page.getTotal(),
                (int) page.getCurrent(),
                (int) page.getSize()
        );

        return ApiResponse.success(response);
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "生成下载令牌")
    public ApiResponse<String> generateDownloadToken(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam(required = false) String approvalId) {
        String token = deliveryService.generateDownloadToken(id, userId, approvalId);
        return ApiResponse.success(token);
    }

    @PostMapping("/records/{id}/unpublish")
    @Operation(summary = "下架数据集")
    public ApiResponse<Void> unpublish(@PathVariable Long id) {
        deliveryService.unpublish(id);
        return ApiResponse.success();
    }
}
