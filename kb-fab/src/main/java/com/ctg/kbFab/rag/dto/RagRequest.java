package com.ctg.kbFab.rag.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * RAG问答请求
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
public class RagRequest {

    @NotBlank(message = "question is required")
    private String question;

    /** 会话ID */
    private String sessionId;

    /** 领域过滤 */
    private String domain;

    /** 检索数量 */
    private Integer topK = 5;

    /** 是否流式返回 */
    private Boolean stream = false;

    /** 额外参数 */
    private Map<String, Object> parameters;
}
