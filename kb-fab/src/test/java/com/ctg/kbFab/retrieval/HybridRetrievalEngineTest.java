package com.ctg.kbFab.retrieval;

import com.ctg.kbFab.retrieval.engine.HybridRetrievalEngine;
import com.ctg.kbFab.retrieval.dto.RetrievalRequest;
import com.ctg.kbFab.retrieval.dto.RetrievalResult;
import com.ctg.kbFab.storage.graph.GraphStore;
import com.ctg.kbFab.storage.graph.impl.UgeGraphStore;
import com.ctg.kbFab.storage.mapper.KnowledgeEntryMapper;
import com.ctg.kbFab.storage.vector.VectorStore;
import com.ctg.kbFab.storage.vector.impl.ProtonVectorStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 混合检索引擎测试
 */
class HybridRetrievalEngineTest {

    private HybridRetrievalEngine engine;
    private VectorStore vectorStore;
    private GraphStore graphStore;
    private KnowledgeEntryMapper knowledgeMapper;

    @BeforeEach
    void setUp() {
        vectorStore = new ProtonVectorStore();
        graphStore = new UgeGraphStore();
        knowledgeMapper = Mockito.mock(KnowledgeEntryMapper.class);

        engine = new HybridRetrievalEngine(vectorStore, graphStore, knowledgeMapper);

        // 初始化向量索引
        vectorStore.createIndex("kb_knowledge", 768, "cosine");
    }

    @Test
    void testVectorSearch() {
        // 插入测试向量
        float[] vector = new float[768];
        vector[0] = 1.0f;
        Map<String, Object> meta = new HashMap<>();
        meta.put("knowledgeId", "test-001");
        meta.put("title", "测试知识");
        meta.put("domain", "engineering");
        vectorStore.insert("kb_knowledge", vector, meta);

        Mockito.when(knowledgeMapper.selectById("test-001")).thenReturn(null);

        RetrievalRequest request = new RetrievalRequest();
        request.setQuery("测试");
        request.setMode("VECTOR");
        request.setTopK(5);
        request.setMinScore(0.0);

        List<RetrievalResult> results = engine.retrieve(request);
        assertNotNull(results);
    }

    @Test
    void testGraphSearch() {
        // 插入测试节点
        Map<String, Object> props = new HashMap<>();
        props.put("knowledgeId", "test-002");
        props.put("title", "图谱节点");
        props.put("type", "ENTITY");
        props.put("domain", "engineering");
        graphStore.createNode("TEST", props);

        Mockito.when(knowledgeMapper.selectById("test-002")).thenReturn(null);

        RetrievalRequest request = new RetrievalRequest();
        request.setQuery("图谱");
        request.setMode("GRAPH");
        request.setTopK(5);
        request.setMinScore(0.0);

        List<RetrievalResult> results = engine.retrieve(request);
        assertNotNull(results);
    }

    @Test
    void testHybridSearch() {
        RetrievalRequest request = new RetrievalRequest();
        request.setQuery("混合检索测试");
        request.setMode("HYBRID");
        request.setTopK(5);
        request.setMinScore(0.0);

        List<RetrievalResult> results = engine.retrieve(request);
        assertNotNull(results);
    }

    @Test
    void testRetrievalWithMinScore() {
        RetrievalRequest request = new RetrievalRequest();
        request.setQuery("测试");
        request.setMode("VECTOR");
        request.setTopK(5);
        request.setMinScore(0.99); // 高阈值

        List<RetrievalResult> results = engine.retrieve(request);
        // 高阈值应过滤掉大部分结果
        assertNotNull(results);
    }
}
