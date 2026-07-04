package com.ctg.aiFab.gateway.service;

import com.ctg.aiFab.gateway.dto.ChatRequest;
import com.ctg.aiFab.gateway.dto.ChatResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * AI服务 - 调用集团AI中台
 *
 * @author Developer
 * @since 2026-07-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final WebClient.Builder webClientBuilder;

    @Value("${ai-platform.base-url:http://ai-platform.ctg.com}")
    private String aiPlatformBaseUrl;

    @Value("${ai-platform.api-key:default-api-key}")
    private String apiKey;

    @CircuitBreaker(name = "aiPlatform", fallbackMethod = "chatFallback")
    @TimeLimiter(name = "aiPlatform")
    @Retry(name = "aiPlatform")
    public CompletableFuture<ChatResponse> chat(ChatRequest request, Long userId, String username) {
        long startTime = System.currentTimeMillis();

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", request.getModel() != null ? request.getModel() : "gpt-4");
        requestBody.put("messages", new Object[]{
            Map.of("role", "user", "content", request.getQuestion())
        });
        requestBody.put("temperature", request.getTemperature());
        requestBody.put("max_tokens", request.getMaxTokens());

        // 调用AI中台API
        return webClientBuilder.build()
            .post()
            .uri(aiPlatformBaseUrl + "/v1/chat/completions")
            .header("Authorization", "Bearer " + apiKey)
            .header("X-User-Id", String.valueOf(userId))
            .header("X-Username", username)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(Map.class)
            .map(response -> {
                long latencyMs = System.currentTimeMillis() - startTime;

                // 解析响应
                ChatResponse chatResponse = new ChatResponse();
                if (response != null && response.containsKey("choices")) {
                    Object[] choices = (Object[]) response.get("choices");
                    if (choices.length > 0) {
                        Map<String, Object> choice = (Map<String, Object>) choices[0];
                        Map<String, Object> message = (Map<String, Object>) choice.get("message");
                        chatResponse.setAnswer((String) message.get("content"));
                    }
                }

                chatResponse.setModel(request.getModel() != null ? request.getModel() : "gpt-4");
                chatResponse.setTokensUsed(request.getMaxTokens());
                chatResponse.setLatencyMs(latencyMs);

                return chatResponse;
            })
            .toFuture();
    }

    /**
     * 熔断降级方法
     */
    public CompletableFuture<ChatResponse> chatFallback(ChatRequest request, Long userId, String username, Throwable t) {
        log.error("AI中台调用失败，触发熔断降级: {}", t.getMessage());
        ChatResponse errorResponse = new ChatResponse();
        errorResponse.setAnswer("抱歉，AI服务暂时不可用，请稍后重试。");
        errorResponse.setModel("fallback");
        errorResponse.setTokensUsed(0);
        errorResponse.setLatencyMs(0);
        return CompletableFuture.completedFuture(errorResponse);
    }
}
