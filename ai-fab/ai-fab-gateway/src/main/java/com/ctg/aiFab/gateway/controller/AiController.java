package com.ctg.aiFab.gateway.controller;

import com.ctg.aiFab.gateway.common.dto.ApiResponse;
import com.ctg.aiFab.gateway.dto.ChatRequest;
import com.ctg.aiFab.gateway.dto.ChatResponse;
import com.ctg.aiFab.gateway.service.AiService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * AI智能问答控制器
 *
 * @author Developer
 * @since 2026-07-01
 */
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/chat")
    @PreAuthorize("hasAuthority('ai:chat')")
    public ApiResponse<ChatResponse> chat(@Valid @RequestBody ChatRequest request,
                                         HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String username = (String) httpRequest.getAttribute("username");

        ChatResponse response = aiService.chat(request, userId, username).join();
        return ApiResponse.success(response);
    }
}
