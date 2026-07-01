package com.ctg.kbFab.storage.vector.impl;

import com.ctg.kbFab.storage.vector.VectorStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 国产向量库存储实现（Proton兼容层）
 * 实际生产环境需替换为真实国产向量库连接
 *
 * @author Developer
 * @since 2026-07-01
 */
@Slf4j
@Component
public class ProtonVectorStore implements VectorStore {

    private final Map<String, List<VectorEntry>> indexes = new ConcurrentHashMap<>();

    private record VectorEntry(String id, float[] vector, Map<String, Object> metadata) {}

    @Override
    public void createIndex(String indexName, int dimension, String metricType) {
        indexes.putIfAbsent(indexName, Collections.synchronizedList(new ArrayList<>()));
        log.info("Created vector index: {} (dim={}, metric={})", indexName, dimension, metricType);
    }

    @Override
    public String insert(String indexName, float[] vector, Map<String, Object> metadata) {
        String id = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        List<VectorEntry> index = indexes.computeIfAbsent(indexName, k -> Collections.synchronizedList(new ArrayList<>()));
        index.add(new VectorEntry(id, vector, metadata != null ? new HashMap<>(metadata) : new HashMap<>()));
        log.debug("Inserted vector {} into index {}", id, indexName);
        return id;
    }

    @Override
    public List<String> batchInsert(String indexName, List<float[]> vectors, List<Map<String, Object>> metadataList) {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < vectors.size(); i++) {
            Map<String, Object> metadata = (metadataList != null && i < metadataList.size()) ? metadataList.get(i) : null;
            ids.add(insert(indexName, vectors.get(i), metadata));
        }
        return ids;
    }

    @Override
    public List<VectorSearchResult> search(String indexName, float[] queryVector, int topK) {
        List<VectorEntry> index = indexes.getOrDefault(indexName, Collections.emptyList());
        return index.stream()
                .map(entry -> new VectorSearchResult(
                        entry.id(),
                        cosineSimilarity(queryVector, entry.vector()),
                        entry.metadata()))
                .sorted((a, b) -> Float.compare(b.score(), a.score()))
                .limit(topK)
                .collect(Collectors.toList());
    }

    @Override
    public List<VectorSearchResult> searchWithFilter(String indexName, float[] queryVector, int topK, Map<String, Object> filter) {
        List<VectorEntry> index = indexes.getOrDefault(indexName, Collections.emptyList());
        return index.stream()
                .filter(entry -> matchesFilter(entry.metadata(), filter))
                .map(entry -> new VectorSearchResult(
                        entry.id(),
                        cosineSimilarity(queryVector, entry.vector()),
                        entry.metadata()))
                .sorted((a, b) -> Float.compare(b.score(), a.score()))
                .limit(topK)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String indexName, String vectorId) {
        List<VectorEntry> index = indexes.get(indexName);
        if (index != null) {
            index.removeIf(entry -> entry.id().equals(vectorId));
        }
    }

    private float cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) return 0.0f;
        float dotProduct = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        float denominator = (float) (Math.sqrt(normA) * Math.sqrt(normB));
        return denominator == 0 ? 0 : dotProduct / denominator;
    }

    private boolean matchesFilter(Map<String, Object> metadata, Map<String, Object> filter) {
        if (filter == null || filter.isEmpty()) return true;
        for (Map.Entry<String, Object> entry : filter.entrySet()) {
            Object metaValue = metadata.get(entry.getKey());
            if (!entry.getValue().equals(metaValue)) return false;
        }
        return true;
    }
}
