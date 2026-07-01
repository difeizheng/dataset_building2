package com.ctg.kbFab.rag.service.impl;

import com.ctg.kbFab.rag.dto.RagRequest;
import com.ctg.kbFab.rag.dto.RagResponse;
import com.ctg.kbFab.rag.engine.RagEngine;
import com.ctg.kbFab.rag.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * RAG问答服务实现
 *
 * @author Developer
 * @since 2026-07-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagServiceImpl implements RagService {

    private final RagEngine ragEngine;

    @Override
    public RagResponse answer(RagRequest request) {
        log.info("Processing RAG question: {}", request.getQuestion());
        return ragEngine.answer(request);
    }

    @Override
    public RagResponse simpleAnswer(String question) {
        RagRequest request = new RagRequest();
        request.setQuestion(question);
        request.setTopK(5);
        return answer(request);
    }
}
