package com.ctg.kbFab.rag.dto;

import lombok.Data;
import java.util.List;

/**
 * RAG问答响应
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
public class RagResponse {

    /** 回答内容 */
    private String answer;

    /** 引用的知识条目 */
    private List<Reference> references;

    /** 置信度 */
    private Double confidence;

    /** 处理耗时(ms) */
    private Long durationMs;

    /** 会话ID */
    private String sessionId;

    @Data
    public static class Reference {
        private String knowledgeId;
        private String title;
        private String content;
        private Double score;
        private String source;
    }
}
