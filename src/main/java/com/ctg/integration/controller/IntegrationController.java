package com.ctg.integration.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ctg.integration.dto.integration.*;
import com.ctg.integration.service.IntegrationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 系统集成控制器
 * 提供各外部系统集成调用接口
 *
 * @author CTG
 * @since 2026-07-01
 */
@Slf4j
@RestController
@RequestMapping("/integration")
@RequiredArgsConstructor
@Tag(name = "系统集成", description = "外部系统集成调用")
public class IntegrationController {

    private final IntegrationService integrationService;

    @PostMapping("/ai-platform")
    @Operation(summary = "AI中台调用", description = "调用AI中台进行模型推理")
    public ResponseEntity<AIPlatformResponse> callAIPlatform(@Valid @RequestBody AIPlatformRequest request) {
        log.info("调用AI中台: modelId={}", request.getModelId());
        AIPlatformResponse response = integrationService.callAIPlatform(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/llm")
    @Operation(summary = "大模型平台调用", description = "调用大模型平台进行问答生成")
    public ResponseEntity<LLMResponse> callLLMPlatform(@Valid @RequestBody LLMRequest request) {
        log.info("调用大模型平台: model={}", request.getModel());
        LLMResponse response = integrationService.callLLMPlatform(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/big-data")
    @Operation(summary = "大数据平台调用", description = "调用大数据平台进行数据接入与分发")
    public ResponseEntity<BigDataResponse> accessBigDataPlatform(@Valid @RequestBody BigDataRequest request) {
        log.info("调用大数据平台: dataSourceId={}", request.getDataSourceId());
        BigDataResponse response = integrationService.accessBigDataPlatform(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/xingyun")
    @Operation(summary = "三峡行云调用", description = "调用三峡行云进行审批流程")
    public ResponseEntity<XingyunResponse> callXingyunApproval(@Valid @RequestBody XingyunRequest request) {
        log.info("调用三峡行云: processId={}", request.getProcessId());
        XingyunResponse response = integrationService.callXingyunApproval(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/wps")
    @Operation(summary = "WPS服务调用", description = "调用WPS服务中台进行文档处理")
    public ResponseEntity<WPSResponse> callWPSService(@Valid @RequestBody WPSRequest request) {
        log.info("调用WPS服务: operationType={}", request.getOperationType());
        WPSResponse response = integrationService.callWPSService(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signature")
    @Operation(summary = "签章系统调用", description = "调用数字化签章系统进行签章")
    public ResponseEntity<SignatureResponse> callSignatureService(@Valid @RequestBody SignatureRequest request) {
        log.info("调用签章系统: documentId={}", request.getDocumentId());
        SignatureResponse response = integrationService.callSignatureService(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    @Operation(summary = "系统状态", description = "获取所有集成系统状态")
    public ResponseEntity<List<SystemStatusVO>> getAllSystemStatus() {
        log.debug("获取所有集成系统状态");
        List<SystemStatusVO> statusList = integrationService.getAllSystemStatus();
        return ResponseEntity.ok(statusList);
    }
}
