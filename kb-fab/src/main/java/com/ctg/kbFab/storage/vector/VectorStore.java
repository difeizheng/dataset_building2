package com.ctg.kbFab.storage.vector;

import java.util.List;
import java.util.Map;

/**
 * 向量库存储接口（国产向量库抽象层）
 * 支持 Proton/PGVector/Milvus国产版 等
 *
 * @author Developer
 * @since 2026-07-01
 */
public interface VectorStore {

    /**
     * 创建向量索引
     */
    void createIndex(String indexName, int dimension, String metricType);

    /**
     * 插入向量
     */
    String insert(String indexName, float[] vector, Map<String, Object> metadata);

    /**
     * 批量插入向量
     */
    List<String> batchInsert(String indexName, List<float[]> vectors, List<Map<String, Object>> metadataList);

    /**
     * 向量搜索（KNN）
     */
    List<VectorSearchResult> search(String indexName, float[] queryVector, int topK);

    /**
     * 带过滤条件的向量搜索
     */
    List<VectorSearchResult> searchWithFilter(String indexName, float[] queryVector, int topK, Map<String, Object> filter);

    /**
     * 删除向量
     */
    void delete(String indexName, String vectorId);

    /**
     * 向量搜索结果
     */
    record VectorSearchResult(String id, float score, Map<String, Object> metadata) {}
}
