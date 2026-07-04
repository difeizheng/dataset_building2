package com.ctg.kbFab.retrieval.service.impl;

import com.ctg.kbFab.retrieval.dto.RetrievalRequest;
import com.ctg.kbFab.retrieval.dto.RetrievalResult;
import com.ctg.kbFab.retrieval.engine.HybridRetrievalEngine;
import com.ctg.kbFab.retrieval.service.RetrievalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 检索服务实现
 *
 * @author Developer
 * @since 2026-07-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RetrievalServiceImpl implements RetrievalService {

    private final HybridRetrievalEngine hybridRetrievalEngine;

    @Override
    public List<RetrievalResult> retrieve(RetrievalRequest request) {
        log.info("Executing retrieval: mode={}, query={}", request.getMode(), request.getQuery());
        return hybridRetrievalEngine.retrieve(request);
    }

    @Override
    public List<RetrievalResult> simpleRetrieve(String query, Integer topK) {
        RetrievalRequest request = new RetrievalRequest();
        request.setQuery(query);
        request.setMode("HYBRID");
        request.setTopK(topK != null ? topK : 10);
        request.setRerank(true);
        return retrieve(request);
    }
}
