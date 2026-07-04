package com.ctg.kbFab.storage.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.kbFab.storage.dto.CreateKnowledgeRequest;
import com.ctg.kbFab.storage.entity.KnowledgeEntry;

/**
 * 知识存储服务接口
 *
 * @author Developer
 * @since 2026-07-01
 */
public interface KnowledgeStorageService {

    /**
     * 存储知识条目（同时写入达梦+图库+向量库）
     */
    KnowledgeEntry storeKnowledge(CreateKnowledgeRequest request);

    /**
     * 获取知识条目
     */
    KnowledgeEntry getKnowledge(String id);

    /**
     * 分页查询知识条目
     */
    Page<KnowledgeEntry> listKnowledge(Integer page, Integer size, String domain);

    /**
     * 更新知识条目
     */
    KnowledgeEntry updateKnowledge(String id, CreateKnowledgeRequest request);

    /**
     * 删除知识条目（同时从图库+向量库删除）
     */
    void deleteKnowledge(String id);

    /**
     * 发布知识条目
     */
    void publishKnowledge(String id);
}
