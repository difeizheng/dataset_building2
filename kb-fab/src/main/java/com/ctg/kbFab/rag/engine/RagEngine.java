package com.ctg.kbFab.rag.engine;

import com.ctg.kbFab.rag.dto.RagRequest;
import com.ctg.kbFab.rag.dto.RagResponse;
import com.ctg.kbFab.retrieval.dto.RetrievalRequest;
import com.ctg.kbFab.retrieval.dto.RetrievalResult;
import com.ctg.kbFab.retrieval.service.RetrievalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG问答引擎
 * 实现检索增强生成（Retrieval-Augmented Generation）
 *
 * @author Developer
 * @since 2026-07-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEngine {

    private final RetrievalService retrievalService;

    /**
     * 执行RAG问答
     */
    public RagResponse answer(RagRequest request) {
        long startTime = System.currentTimeMillis();

        // 1. 检索相关知识
        RetrievalRequest retrievalRequest = new RetrievalRequest();
        retrievalRequest.setQuery(request.getQuestion());
        retrievalRequest.setMode("HYBRID");
        retrievalRequest.setTopK(request.getTopK());
        retrievalRequest.setDomain(request.getDomain());
        retrievalRequest.setRerank(true);

        List<RetrievalResult> retrievedDocs = retrievalService.retrieve(retrievalRequest);
        log.debug("Retrieved {} documents for question: {}", retrievedDocs.size(), request.getQuestion());

        // 2. 构建上下文
        String context = buildContext(retrievedDocs);

        // 3. 生成回答（模拟LLM生成）
        String answer = generateAnswer(request.getQuestion(), context);

        // 4. 构建响应
        RagResponse response = new RagResponse();
        response.setAnswer(answer);
        response.setReferences(buildReferences(retrievedDocs));
        response.setConfidence(calculateConfidence(retrievedDocs));
        response.setSessionId(request.getSessionId());

        long duration = System.currentTimeMillis() - startTime;
        response.setDurationMs(duration);

        log.info("RAG answer generated in {}ms, confidence: {}", duration, response.getConfidence());
        return response;
    }

    /**
     * 构建上下文
     */
    private String buildContext(List<RetrievalResult> documents) {
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < documents.size(); i++) {
            RetrievalResult doc = documents.get(i);
            context.append(String.format("[%d] %s\n%s\n\n",
                    i + 1,
                    doc.getTitle() != null ? doc.getTitle() : "",
                    doc.getContent() != null ? doc.getContent() : ""));
        }
        return context.toString();
    }

    /**
     * 生成回答（模拟LLM）
     * 生产环境应替换为真实的LLM调用
     */
    private String generateAnswer(String question, String context) {
        // 简单的基于检索结果的回答生成
        if (context.isEmpty()) {
            return "抱歉，我没有找到相关的知识来回答您的问题。";
        }

        // 提取关键信息构建回答
        String[] contextParts = context.split("\n\n");
        StringBuilder answer = new StringBuilder();

        answer.append("根据知识库中的信息：\n\n");

        for (int i = 0; i < Math.min(3, contextParts.length); i++) {
            String part = contextParts[i].trim();
            if (!part.isEmpty()) {
                // 提取内容部分（跳过编号和标题）
                String[] lines = part.split("\n", 2);
                if (lines.length > 1) {
                    answer.append("- ").append(lines[1].substring(0, Math.min(200, lines[1].length())));
                    if (lines[1].length() > 200) {
                        answer.append("...");
                    }
                    answer.append("\n");
                }
            }
        }

        answer.append("\n以上信息来自知识库的检索结果，供您参考。");

        return answer.toString();
    }

    /**
     * 构建引用列表
     */
    private List<RagResponse.Reference> buildReferences(List<RetrievalResult> documents) {
        return documents.stream()
                .map(doc -> {
                    RagResponse.Reference ref = new RagResponse.Reference();
                    ref.setKnowledgeId(doc.getKnowledgeId());
                    ref.setTitle(doc.getTitle());
                    ref.setContent(doc.getContent());
                    ref.setScore(doc.getScore());
                    ref.setSource(doc.getSource());
                    return ref;
                })
                .collect(Collectors.toList());
    }

    /**
     * 计算置信度
     */
    private Double calculateConfidence(List<RetrievalResult> documents) {
        if (documents.isEmpty()) {
            return 0.0;
        }
        double avgScore = documents.stream()
                .mapToDouble(RetrievalResult::getScore)
                .average()
                .orElse(0.0);
        return Math.min(1.0, avgScore);
    }
}
