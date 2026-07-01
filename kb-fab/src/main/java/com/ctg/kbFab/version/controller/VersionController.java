package com.ctg.kbFab.version.controller;

import com.ctg.kbFab.common.dto.ApiResponse;
import com.ctg.kbFab.storage.entity.KnowledgeEntry;
import com.ctg.kbFab.version.entity.KnowledgeVersion;
import com.ctg.kbFab.version.service.VersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/kb/version")
@RequiredArgsConstructor
@Tag(name = "知识版本", description = "知识版本管理API")
public class VersionController {

    private final VersionService versionService;

    @GetMapping("/{knowledgeId}")
    @Operation(summary = "查询版本历史")
    public ApiResponse<List<KnowledgeVersion>> listVersions(@PathVariable String knowledgeId) {
        return ApiResponse.success(versionService.listVersions(knowledgeId));
    }

    @GetMapping("/detail/{versionId}")
    @Operation(summary = "获取版本详情")
    public ApiResponse<KnowledgeVersion> getVersion(@PathVariable String versionId) {
        return ApiResponse.success(versionService.getVersion(versionId));
    }

    @PostMapping("/{knowledgeId}/rollback/{targetVersion}")
    @Operation(summary = "回滚到指定版本")
    public ApiResponse<KnowledgeEntry> rollback(
            @PathVariable String knowledgeId,
            @PathVariable Integer targetVersion) {
        return ApiResponse.success(versionService.rollback(knowledgeId, targetVersion));
    }
}
