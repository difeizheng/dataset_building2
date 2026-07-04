package com.ctg.kbFab.retrieval.engine;

import com.ctg.kbFab.retrieval.dto.RetrievalRequest;
import com.ctg.kbFab.retrieval.dto.RetrievalResult;
import com.ctg.kbFab.storage.entity.KnowledgeEntry;
import com.ctg.kbFab.storage.graph.GraphStore;
import com.ctg.kbFab.storage.mapper.KnowledgeEntryMapper;
import com.ctg.kbFab.storage.vector.VectorStore;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 混合检索引擎
 * 实现向量检索 + 图谱检索 + 全文检索 + 重排序
 *
 * @author Developer
 * @since 2026-07-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HybridRetrievalEngine {

    private final VectorStore vectorStore;
    private final GraphStore graphStore;
    private final KnowledgeEntryMapper knowledgeEntryMapper;

    private static final String VECTOR_INDEX = "kb_knowledge";

    /**
     * 执行混合检索
     */
    public List<RetrievalResult> retrieve(RetrievalRequest request) {
        long startTime = System.currentTimeMillis();

        List<RetrievalResult> results = new ArrayList<>();

        switch (request.getMode().toUpperCase()) {
            case "VECTOR":
                results = vectorSearch(request);
                break;
            case "GRAPH":
                results = graphSearch(request);
                break;
            case "FULLTEXT":
                results = fulltextSearch(request);
                break;
            case "HYBRID":
            default:
                results = hybridSearch(request);
                break;
        }

        // 应用最小得分过滤
        results = results.stream()
                .filter(r -> r.getScore() >= request.getMinScore())
                .collect(Collectors.toList());

        // 重排序
        if (request.getRerank() && results.size() > 1) {
            results = rerank(results, request.getQuery());
        }

        // 限制返回数量
        if (results.size() > request.getTopK()) {
            results = results.subList(0, request.getTopK());
        }

        long duration = System.currentTimeMillis() - startTime;
        log.debug("Retrieval completed in {}ms, found {} results", duration, results.size());

        return results;
    }

    /**
     * 向量检索
     */
    private List<RetrievalResult> vectorSearch(RetrievalRequest request) {
        float[] queryVector = generateSimpleVector(request.getQuery());
        Map<String, Object> filter = new HashMap<>();
        if (request.getDomain() != null) {
            filter.put("domain", request.getDomain());
        }

        List<VectorStore.VectorSearchResult> vectorResults =
                vectorStore.searchWithFilter(VECTOR_INDEX, queryVector, request.getTopK() * 2, filter);

        return vectorResults.stream()
                .map(vr -> {
                    RetrievalResult result = new RetrievalResult();
                    result.setKnowledgeId((String) vr.metadata().get("knowledgeId"));
                    result.setTitle((String) vr.metadata().get("title"));
                    result.setSource("VECTOR");
                    result.setVectorScore((double) vr.score());
                    result.setScore((double) vr.score());

                    // 加载完整内容
                    loadFullContent(result);
                    return result;
                })
                .collect(Collectors.toList());
    }

    /**
     * 图谱检索
     */
    private List<RetrievalResult> graphSearch(RetrievalRequest request) {
        // 基于关键词在图库中查找相关节点
        List<Map<String, Object>> nodes = graphStore.getSubGraph(request.getQuery(), 2, request.getTopK() * 2);

        return nodes.stream()
                .map(node -> {
                    RetrievalResult result = new RetrievalResult();
                    result.setKnowledgeId((String) node.get("knowledgeId"));
                    result.setTitle((String) node.get("title"));
                    result.setKnowledgeType((String) node.get("type"));
                    result.setDomain((String) node.get("domain"));
                    result.setSource("GRAPH");
                    result.setGraphScore(0.8); // 图谱匹配得分
                    result.setScore(0.8);

                    loadFullContent(result);
                    return result;
                })
                .filter(r -> r.getKnowledgeId() != null)
                .collect(Collectors.toList());
    }

    /**
     * 全文检索
     */
    private List<RetrievalResult> fulltextSearch(RetrievalRequest request) {
        LambdaQueryWrapper<KnowledgeEntry> wrapper = new LambdaQueryWrapper<>();

        // 关键词匹配
        wrapper.and(w -> w
                .like(KnowledgeEntry::getTitle, request.getQuery())
                .or()
                .like(KnowledgeEntry::getContent, request.getQuery()));

        if (request.getDomain() != null) {
            wrapper.eq(KnowledgeEntry::getDomain, request.getDomain());
        }

        if (request.getKnowledgeTypes() != null && !request.getKnowledgeTypes().isEmpty()) {
            wrapper.in(KnowledgeEntry::getKnowledgeType, request.getKnowledgeTypes());
        }

        wrapper.last("LIMIT " + (request.getTopK() * 2));

        List<KnowledgeEntry> entries = knowledgeEntryMapper.selectList(wrapper);

        return entries.stream()
                .map(entry -> {
                    RetrievalResult result = new RetrievalResult();
                    result.setKnowledgeId(entry.getId());
                    result.setTitle(entry.getTitle());
                    result.setContent(entry.getContent());
                    result.setKnowledgeType(entry.getKnowledgeType().getCode());
                    result.setDomain(entry.getDomain());
                    result.setSource("FULLTEXT");
                    result.setFulltextScore(calculateFulltextScore(entry, request.getQuery()));
                    result.setScore(result.getFulltextScore());
                    return result;
                })
                .collect(Collectors.toList());
    }

    /**
     * 混合检索（融合向量+图谱+全文）
     */
    private List<RetrievalResult> hybridSearch(RetrievalRequest request) {
        // 并行执行三种检索
        List<RetrievalResult> vectorResults = vectorSearch(request);
        List<RetrievalResult> graphResults = graphSearch(request);
        List<RetrievalResult> fulltextResults = fulltextSearch(request);

        // 融合得分
        Map<String, RetrievalResult> merged = new HashMap<>();

        // 向量结果（权重0.4）
        for (RetrievalResult r : vectorResults) {
            merged.computeIfAbsent(r.getKnowledgeId(), k -> {
                RetrievalResult nr = new RetrievalResult();
                nr.setKnowledgeId(r.getKnowledgeId());
                nr.setTitle(r.getTitle());
                nr.setContent(r.getContent());
                nr.setKnowledgeType(r.getKnowledgeType());
                nr.setDomain(r.getDomain());
                return nr;
            });
            RetrievalResult existing = merged.get(r.getKnowledgeId());
            existing.setVectorScore(r.getVectorScore());
            existing.setScore(existing.getScore() != null ? existing.getScore() : 0.0 + r.getVectorScore() * 0.4);
        }

        // 图谱结果（权重0.3）
        for (RetrievalResult r : graphResults) {
            merged.computeIfAbsent(r.getKnowledgeId(), k -> {
                RetrievalResult nr = new RetrievalResult();
                nr.setKnowledgeId(r.getKnowledgeId());
                nr.setTitle(r.getTitle());
                nr.setContent(r.getContent());
                nr.setKnowledgeType(r.getKnowledgeType());
                nr.setDomain(r.getDomain());
                return nr;
            });
            RetrievalResult existing = merged.get(r.getKnowledgeId());
            existing.setGraphScore(r.getGraphScore());
            existing.setScore((existing.getScore() != null ? existing.getScore() : 0.0) + r.getGraphScore() * 0.3);
        }

        // 全文结果（权重0.3）
        for (RetrievalResult r : fulltextResults) {
            merged.computeIfAbsent(r.getKnowledgeId(), k -> {
                RetrievalResult nr = new RetrievalResult();
                nr.setKnowledgeId(r.getKnowledgeId());
                nr.setTitle(r.getTitle());
                nr.setContent(r.getContent());
                nr.setKnowledgeType(r.getKnowledgeType());
                nr.setDomain(r.getDomain());
                return nr;
            });
            RetrievalResult existing = merged.get(r.getKnowledgeId());
            existing.setFulltextScore(r.getFulltextScore());
            existing.setScore((existing.getScore() != null ? existing.getScore() : 0.0) + r.getFulltextScore() * 0.3);
        }

        List<RetrievalResult> results = new ArrayList<>(merged.values());
        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        return results;
    }

    /**
     * 重排序（基于查询和内容的匹配度）
     */
    private List<RetrievalResult> rerank(List<RetrievalResult> results, String query) {
        for (RetrievalResult result : results) {
            double rerankScore = calculateRerankScore(result, query);
            result.setRerankScore(rerankScore);
            // 综合得分 = 原始得分 * 0.6 + 重排得分 * 0.4
            result.setScore(result.getScore() * 0.6 + rerankScore * 0.4);
        }
        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        return results;
    }

    /**
     * 计算重排得分
     */
    private double calculateRerankScore(RetrievalResult result, String query) {
        if (result.getContent() == null || query == null) return 0.0;

        String content = result.getContent().toLowerCase();
        String q = query.toLowerCase();

        // 关键词匹配度
        String[] keywords = q.split("\\s+");
        int matchCount = 0;
        for (String keyword : keywords) {
            if (content.contains(keyword)) {
                matchCount++;
            }
        }

        double keywordScore = keywords.length > 0 ? (double) matchCount / keywords.length : 0.0;

        // 标题匹配加分
        double titleBonus = 0.0;
        if (result.getTitle() != null && result.getTitle().toLowerCase().contains(q)) {
            titleBonus = 0.2;
        }

        return Math.min(1.0, keywordScore + titleBonus);
    }

    /**
     * 计算全文检索得分
     */
    private double calculateFulltextScore(KnowledgeEntry entry, String query) {
        String content = entry.getContent().toLowerCase();
        String q = query.toLowerCase();

        // 简单的词频统计
        int count = 0;
        int index = 0;
        while ((index = content.indexOf(q, index)) != -1) {
            count++;
            index += q.length();
        }

        // 归一化得分
        double score = Math.min(1.0, count * 0.1);

        // 标题匹配加分
        if (entry.getTitle() != null && entry.getTitle().toLowerCase().contains(q)) {
            score += 0.3;
        }

        return Math.min(1.0, score);
    }

    /**
     * 加载完整内容
     */
    private void loadFullContent(RetrievalResult result) {
        if (result.getKnowledgeId() != null) {
            KnowledgeEntry entry = knowledgeEntryMapper.selectById(result.getKnowledgeId());
            if (entry != null) {
                result.setContent(entry.getContent());
                result.setKnowledgeType(entry.getKnowledgeType().getCode());
                result.setDomain(entry.getDomain());
            }
        }
    }

    /**
     * 生成简易向量（与StorageServiceImpl保持一致）
     */
    private float[] generateSimpleVector(String text) {
        int dimension = 768;
        float[] vector = new float[dimension];
        if (text == null || text.isEmpty()) {
            return vector;
        }
        for (int i = 0; i < text.length() && i < dimension; i++) {
            vector[i % dimension] += (text.charAt(i) % 100) / 100.0f;
        }
        float norm = 0;
        for (float v : vector) {
            norm += v * v;
        }
        norm = (float) Math.sqrt(norm);
        if (norm > 0) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] /= norm;
            }
        }
        return vector;
    }
}
