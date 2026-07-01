package com.ctg.kbFab.rag;

import com.ctg.kbFab.rag.dto.RagRequest;
import com.ctg.kbFab.rag.dto.RagResponse;
import com.ctg.kbFab.rag.engine.RagEngine;
import com.ctg.kbFab.retrieval.dto.RetrievalResult;
import com.ctg.kbFab.retrieval.service.RetrievalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RAG引擎测试
 */
class RagEngineTest {

    private RagEngine ragEngine;
    private RetrievalService retrievalService;

    @BeforeEach
    void setUp() {
        retrievalService = Mockito.mock(RetrievalService.class);
        ragEngine = new RagEngine(retrievalService);
    }

    @Test
    void testAnswer_withResults() {
        List<RetrievalResult> mockResults = new ArrayList<>();
        RetrievalResult result = new RetrievalResult();
        result.setKnowledgeId("k001");
        result.setTitle("测试知识");
        result.setContent("这是测试内容，包含重要的技术信息。");
        result.setScore(0.85);
        result.setSource("VECTOR");
        mockResults.add(result);

        Mockito.when(retrievalService.retrieve(Mockito.any())).thenReturn(mockResults);

        RagRequest request = new RagRequest();
        request.setQuestion("测试问题");
        request.setTopK(5);

        RagResponse response = ragEngine.answer(request);

        assertNotNull(response);
        assertNotNull(response.getAnswer());
        assertFalse(response.getAnswer().isEmpty());
        assertNotNull(response.getReferences());
        assertEquals(1, response.getReferences().size());
        assertTrue(response.getConfidence() > 0);
        assertNotNull(response.getDurationMs());
    }

    @Test
    void testAnswer_emptyResults() {
        Mockito.when(retrievalService.retrieve(Mockito.any())).thenReturn(new ArrayList<>());

        RagRequest request = new RagRequest();
        request.setQuestion("不存在的问题");

        RagResponse response = ragEngine.answer(request);

        assertNotNull(response);
        assertTrue(response.getAnswer().contains("没有找到"));
        assertEquals(0.0, response.getConfidence());
    }

    @Test
    void testAnswer_multipleReferences() {
        List<RetrievalResult> mockResults = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            RetrievalResult result = new RetrievalResult();
            result.setKnowledgeId("k" + i);
            result.setTitle("知识" + i);
            result.setContent("内容" + i);
            result.setScore(0.9 - i * 0.1);
            mockResults.add(result);
        }

        Mockito.when(retrievalService.retrieve(Mockito.any())).thenReturn(mockResults);

        RagRequest request = new RagRequest();
        request.setQuestion("综合问题");

        RagResponse response = ragEngine.answer(request);

        assertEquals(5, response.getReferences().size());
    }
}
