package com.ctg.kbFab.access.controller;

import com.ctg.kbFab.access.entity.KnowledgeAccess;
import com.ctg.kbFab.access.service.AccessService;
import com.ctg.kbFab.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/kb/access")
@RequiredArgsConstructor
@Tag(name = "知识权限", description = "知识访问权限控制API")
public class AccessController {

    private final AccessService accessService;

    @PostMapping("/grant")
    @Operation(summary = "授予访问权限")
    public ApiResponse<KnowledgeAccess> grantAccess(
            @RequestParam String knowledgeId,
            @RequestParam String principalId,
            @RequestParam(defaultValue = "USER") String principalType,
            @RequestParam(defaultValue = "READ") String accessLevel,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expireTime) {
        return ApiResponse.success(accessService.grantAccess(knowledgeId, principalId, principalType, accessLevel, expireTime));
    }

    @PostMapping("/revoke/{accessId}")
    @Operation(summary = "撤销访问权限")
    public ApiResponse<Void> revokeAccess(@PathVariable String accessId) {
        accessService.revokeAccess(accessId);
        return ApiResponse.success();
    }

    @GetMapping("/check")
    @Operation(summary = "检查访问权限")
    public ApiResponse<Boolean> checkAccess(
            @RequestParam String knowledgeId,
            @RequestParam String principalId,
            @RequestParam(defaultValue = "READ") String requiredLevel) {
        return ApiResponse.success(accessService.checkAccess(knowledgeId, principalId, requiredLevel));
    }

    @GetMapping("/{knowledgeId}")
    @Operation(summary = "查询知识权限列表")
    public ApiResponse<List<KnowledgeAccess>> listAccess(@PathVariable String knowledgeId) {
        return ApiResponse.success(accessService.listAccess(knowledgeId));
    }
}
