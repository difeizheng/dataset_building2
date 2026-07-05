package com.ctg.dataFab.delivery.controller;

import com.ctg.dataFab.access.annotation.AuditDataAccess;
import com.ctg.dataFab.common.dto.ApiResponse;
import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.common.dto.PageResponse;
import com.ctg.dataFab.common.exception.BusinessException;
import com.ctg.dataFab.common.security.MfaService;
import com.ctg.dataFab.delivery.dto.PublishRequest;
import com.ctg.dataFab.delivery.dto.PublishResponse;
import com.ctg.dataFab.delivery.entity.DeliveryRecord;
import com.ctg.dataFab.delivery.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 数据集交付控制器
 * 对应接口：POST /api/v1/delivery/publish, GET /api/v1/delivery/{id}/download
 *
 * L4 端点需要额外 MFA 验证 (X-MFA-Token header)
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
    private final MfaService mfaService;

    @PostMapping("/publish")
    @PreAuthorize("hasRole('ADMIN')")
    @AuditDataAccess(action = "WRITE", resourceType = "DELIVERY")
    @Operation(summary = "发布数据集")
    public ApiResponse<PublishResponse> publish(@Valid @RequestBody PublishRequest request) {
        PublishResponse response = deliveryService.publish(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/records/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'AUDITOR')")
    @AuditDataAccess(action = "READ", resourceType = "DELIVERY")
    @Operation(summary = "获取交付记录详情")
    public ApiResponse<DeliveryRecord> getRecord(@PathVariable Long id) {
        DeliveryRecord record = deliveryService.getRecordById(id);
        return ApiResponse.success(record);
    }

    @GetMapping("/records")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'AUDITOR')")
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

    /**
     * L4 数据下载 — 需要 MFA 验证
     * Header: X-MFA-Token: <totp|hotp|sms>
     */
    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @AuditDataAccess(action = "DOWNLOAD", resourceType = "DELIVERY", dataLevel = "L4")
    @Operation(summary = "生成下载令牌")
    public ApiResponse<String> generateDownloadToken(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam(required = false) String approvalId,
            @RequestHeader(value = "X-MFA-Token", required = false) String mfaToken) {

        // MFA 验证
        if (!mfaService.verifyMFA(mfaToken)) {
            throw new com.ctg.dataFab.common.exception.BusinessException("L4数据访问需要MFA验证");
        }

        String token = deliveryService.generateDownloadToken(id, userId, approvalId);
        return ApiResponse.success(token);
    }

    @PostMapping("/records/{id}/unpublish")
    @PreAuthorize("hasRole('ADMIN')")
    @AuditDataAccess(action = "WRITE", resourceType = "DELIVERY")
    @Operation(summary = "下架数据集")
    public ApiResponse<Void> unpublish(@PathVariable Long id) {
        deliveryService.unpublish(id);
        return ApiResponse.success();
    }
}
