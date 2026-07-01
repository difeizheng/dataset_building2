package com.ctg.kbFab.rag.service;

import com.ctg.kbFab.rag.dto.RagRequest;
import com.ctg.kbFab.rag.dto.RagResponse;

/**
 * RAG问答服务接口
 *
 * @author Developer
 * @since 2026-07-01
 */
public interface RagService {

    /**
     * 执行问答
     */
    RagResponse answer(RagRequest request);

    /**
     * 简单问答
     */
    RagResponse simpleAnswer(String question);
}
