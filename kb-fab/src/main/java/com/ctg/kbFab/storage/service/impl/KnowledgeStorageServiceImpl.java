package com.ctg.kbFab.storage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.kbFab.common.enums.KnowledgeStatus;
import com.ctg.kbFab.common.enums.KnowledgeType;
import com.ctg.kbFab.common.exception.BusinessException;
import com.ctg.kbFab.common.util.Utils;
import com.ctg.kbFab.storage.dto.CreateKnowledgeRequest;
import com.ctg.kbFab.storage.entity.KnowledgeEntry;
import com.ctg.kbFab.storage.graph.GraphStore;
import com.ctg.kbFab.storage.mapper.KnowledgeEntryMapper;
import com.ctg.kbFab.storage.service.KnowledgeStorageService;
import com.ctg.kbFab.storage.vector.VectorStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 知识存储服务实现
 * 三库协同：达梦DM8 + 国产图库 + 国产向量库
 *
 * @author Developer
 * @since 2026-07-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeStorageServiceImpl implements KnowledgeStorageService {

    private final KnowledgeEntryMapper knowledgeEntryMapper;
    private final GraphStore graphStore;
    private final VectorStore vectorStore;
    private final ObjectMapper objectMapper;

    private static final String VECTOR_INDEX = "kb_knowledge";
    private static final int VECTOR_DIMENSION = 768;

    @Override
    @Transactional
    public KnowledgeEntry storeKnowledge(CreateKnowledgeRequest request) {
        KnowledgeEntry entry = new KnowledgeEntry();
        entry.setTitle(request.getTitle());
        entry.setContent(request.getContent());
        entry.setKnowledgeType(KnowledgeType.valueOf(request.getKnowledgeType().toUpperCase()));
        entry.setStatus(KnowledgeStatus.DRAFT);
        entry.setSourceDocId(request.getSourceDocId());
        entry.setSourceDocName(request.getSourceDocName());
        entry.setDomain(request.getDomain());
        entry.setDataLevel(request.getDataLevel() != null ? request.getDataLevel() : "L1");
        entry.setVersion(1);
        entry.setCreateTime(LocalDateTime.now());
        entry.setUpdateTime(LocalDateTime.now());
        entry.setDeleted(0);

        try {
            if (request.getTags() != null) {
                entry.setTagsJson(objectMapper.writeValueAsString(request.getTags()));
            }
        } catch (Exception e) {
            log.warn("Failed to serialize tags", e);
        }

        // 1. 写入达梦数据库
        knowledgeEntryMapper.insert(entry);
        log.info("Stored knowledge entry in DM: {}", entry.getId());

        // 2. 写入向量库（生成简易向量）
        float[] vector = generateSimpleVector(request.getContent());
        Map<String, Object> vectorMeta = new HashMap<>();
        vectorMeta.put("knowledgeId", entry.getId());
        vectorMeta.put("title", request.getTitle());
        vectorMeta.put("domain", request.getDomain());
        vectorMeta.put("type", request.getKnowledgeType());
        vectorMeta.put("dataLevel", entry.getDataLevel());
        String vectorId = vectorStore.insert(VECTOR_INDEX, vector, vectorMeta);
        entry.setVectorId(vectorId);

        // 3. 写入图库
        Map<String, Object> nodeProps = new HashMap<>();
        nodeProps.put("knowledgeId", entry.getId());
        nodeProps.put("title", request.getTitle());
        nodeProps.put("type", request.getKnowledgeType());
        nodeProps.put("domain", request.getDomain());
        nodeProps.put("dataLevel", entry.getDataLevel());
        String graphNodeId = graphStore.createNode(request.getKnowledgeType().toUpperCase(), nodeProps);
        entry.setGraphNodeId(graphNodeId);

        // 更新向量ID和图谱节点ID
        knowledgeEntryMapper.updateById(entry);
        log.info("Knowledge entry fully stored: id={}, vectorId={}, graphNodeId={}",
                entry.getId(), vectorId, graphNodeId);

        return entry;
    }

    @Override
    public KnowledgeEntry getKnowledge(String id) {
        return knowledgeEntryMapper.selectById(id);
    }

    @Override
    public Page<KnowledgeEntry> listKnowledge(Integer page, Integer size, String domain) {
        Page<KnowledgeEntry> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<KnowledgeEntry> wrapper = new LambdaQueryWrapper<>();
        if (Utils.isNotEmpty(domain)) {
            wrapper.eq(KnowledgeEntry::getDomain, domain);
        }
        wrapper.orderByDesc(KnowledgeEntry::getCreateTime);
        return knowledgeEntryMapper.selectPage(pageParam, wrapper);
    }

    @Override
    @Transactional
    public KnowledgeEntry updateKnowledge(String id, CreateKnowledgeRequest request) {
        KnowledgeEntry entry = getKnowledge(id);
        if (entry == null) {
            throw new BusinessException("Knowledge entry not found: " + id);
        }

        entry.setTitle(request.getTitle());
        entry.setContent(request.getContent());
        entry.setKnowledgeType(KnowledgeType.valueOf(request.getKnowledgeType().toUpperCase()));
        entry.setDomain(request.getDomain());
        entry.setVersion(entry.getVersion() + 1);
        entry.setUpdateTime(LocalDateTime.now());

        try {
            if (request.getTags() != null) {
                entry.setTagsJson(objectMapper.writeValueAsString(request.getTags()));
            }
        } catch (Exception e) {
            log.warn("Failed to serialize tags", e);
        }

        // 更新向量库
        if (entry.getVectorId() != null) {
            vectorStore.delete(VECTOR_INDEX, entry.getVectorId());
        }
        float[] vector = generateSimpleVector(request.getContent());
        Map<String, Object> vectorMeta = new HashMap<>();
        vectorMeta.put("knowledgeId", entry.getId());
        vectorMeta.put("title", request.getTitle());
        String vectorId = vectorStore.insert(VECTOR_INDEX, vector, vectorMeta);
        entry.setVectorId(vectorId);

        // 更新图库
        if (entry.getGraphNodeId() != null) {
            Map<String, Object> props = new HashMap<>();
            props.put("title", request.getTitle());
            props.put("domain", request.getDomain());
            graphStore.updateNode(entry.getGraphNodeId(), props);
        }

        knowledgeEntryMapper.updateById(entry);
        log.info("Updated knowledge entry: {}, version: {}", id, entry.getVersion());
        return entry;
    }

    @Override
    @Transactional
    public void deleteKnowledge(String id) {
        KnowledgeEntry entry = getKnowledge(id);
        if (entry == null) {
            throw new BusinessException("Knowledge entry not found: " + id);
        }

        // 从向量库删除
        if (entry.getVectorId() != null) {
            vectorStore.delete(VECTOR_INDEX, entry.getVectorId());
        }
        // 从图库删除
        if (entry.getGraphNodeId() != null) {
            graphStore.deleteNode(entry.getGraphNodeId());
        }
        // 从达梦删除
        knowledgeEntryMapper.deleteById(id);
        log.info("Deleted knowledge entry: {}", id);
    }

    @Override
    @Transactional
    public void publishKnowledge(String id) {
        KnowledgeEntry entry = getKnowledge(id);
        if (entry == null) {
            throw new BusinessException("Knowledge entry not found: " + id);
        }
        entry.setStatus(KnowledgeStatus.PUBLISHED);
        entry.setUpdateTime(LocalDateTime.now());
        knowledgeEntryMapper.updateById(entry);
        log.info("Published knowledge entry: {}", id);
    }

    /**
     * 生成简易文本向量（用于演示）
     * 生产环境应替换为真实的Embedding模型调用
     */
    private float[] generateSimpleVector(String text) {
        float[] vector = new float[VECTOR_DIMENSION];
        if (text == null || text.isEmpty()) {
            return vector;
        }
        // 基于字符hash的简易向量化
        for (int i = 0; i < text.length() && i < VECTOR_DIMENSION; i++) {
            vector[i % VECTOR_DIMENSION] += (text.charAt(i) % 100) / 100.0f;
        }
        // 归一化
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
