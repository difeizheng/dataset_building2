package com.ctg.kbFab.storage.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.kbFab.common.dto.ApiResponse;
import com.ctg.kbFab.storage.dto.CreateKnowledgeRequest;
import com.ctg.kbFab.storage.entity.KnowledgeEntry;
import com.ctg.kbFab.storage.service.KnowledgeStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 知识存储控制器
 *
 * @author Developer
 * @since 2026-07-01
 */
@RestController
@RequestMapping("/api/v1/kb/storage")
@RequiredArgsConstructor
@Tag(name = "知识存储", description = "知识存储管理API（达梦+图库+向量库三库协同）")
public class StorageController {

    private final KnowledgeStorageService knowledgeStorageService;

    @PostMapping("/knowledge")
    @Operation(summary = "存储知识条目")
    public ApiResponse<KnowledgeEntry> storeKnowledge(@Valid @RequestBody CreateKnowledgeRequest request) {
        KnowledgeEntry entry = knowledgeStorageService.storeKnowledge(request);
        return ApiResponse.success(entry);
    }

    @GetMapping("/knowledge/{id}")
    @Operation(summary = "获取知识条目")
    public ApiResponse<KnowledgeEntry> getKnowledge(@PathVariable String id) {
        KnowledgeEntry entry = knowledgeStorageService.getKnowledge(id);
        return ApiResponse.success(entry);
    }

    @GetMapping("/knowledge")
    @Operation(summary = "分页查询知识")
    public ApiResponse<Page<KnowledgeEntry>> listKnowledge(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String domain) {
        Page<KnowledgeEntry> entries = knowledgeStorageService.listKnowledge(page, size, domain);
        return ApiResponse.success(entries);
    }

    @PutMapping("/knowledge/{id}")
    @Operation(summary = "更新知识条目")
    public ApiResponse<KnowledgeEntry> updateKnowledge(
            @PathVariable String id,
            @Valid @RequestBody CreateKnowledgeRequest request) {
        KnowledgeEntry entry = knowledgeStorageService.updateKnowledge(id, request);
        return ApiResponse.success(entry);
    }

    @DeleteMapping("/knowledge/{id}")
    @Operation(summary = "删除知识条目")
    public ApiResponse<Void> deleteKnowledge(@PathVariable String id) {
        knowledgeStorageService.deleteKnowledge(id);
        return ApiResponse.success();
    }

    @PostMapping("/knowledge/{id}/publish")
    @Operation(summary = "发布知识条目")
    public ApiResponse<Void> publishKnowledge(@PathVariable String id) {
        knowledgeStorageService.publishKnowledge(id);
        return ApiResponse.success();
    }
}
