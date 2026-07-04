package com.ctg.kbFab.rag.controller;

import com.ctg.kbFab.common.dto.ApiResponse;
import com.ctg.kbFab.rag.dto.RagRequest;
import com.ctg.kbFab.rag.dto.RagResponse;
import com.ctg.kbFab.rag.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * RAG问答控制器
 *
 * @author Developer
 * @since 2026-07-01
 */
@RestController
@RequestMapping("/api/v1/kb/rag")
@RequiredArgsConstructor
@Tag(name = "RAG问答", description = "检索增强生成问答API")
public class RagController {

    private final RagService ragService;

    @PostMapping("/ask")
    @Operation(summary = "执行问答")
    public ApiResponse<RagResponse> answer(@Valid @RequestBody RagRequest request) {
        RagResponse response = ragService.answer(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/simple")
    @Operation(summary = "简单问答")
    public ApiResponse<RagResponse> simpleAnswer(@RequestParam String question) {
        RagResponse response = ragService.simpleAnswer(question);
        return ApiResponse.success(response);
    }
}
