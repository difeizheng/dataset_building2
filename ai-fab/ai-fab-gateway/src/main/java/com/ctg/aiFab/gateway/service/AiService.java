package com.ctg.aiFab.gateway.service;

import com.ctg.aiFab.gateway.dto.ChatRequest;
import com.ctg.aiFab.gateway.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

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

    public ChatResponse chat(ChatRequest request, Long userId, String username) {
        long startTime = System.currentTimeMillis();

        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", request.getModel() != null ? request.getModel() : "gpt-4");
            requestBody.put("messages", new Object[]{
                Map.of("role", "user", "content", request.getQuestion())
            });
            requestBody.put("temperature", request.getTemperature());
            requestBody.put("max_tokens", request.getMaxTokens());

            // 调用AI中台API
            Map<String, Object> response = webClientBuilder.build()
                .post()
                .uri(aiPlatformBaseUrl + "/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("X-User-Id", String.valueOf(userId))
                .header("X-Username", username)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

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
        } catch (Exception e) {
            log.error("AI中台调用失败: {}", e.getMessage(), e);
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setAnswer("抱歉，AI服务暂时不可用，请稍后重试。");
            errorResponse.setModel("error");
            errorResponse.setTokensUsed(0);
            errorResponse.setLatencyMs(System.currentTimeMillis() - startTime);
            return errorResponse;
        }
    }
}
