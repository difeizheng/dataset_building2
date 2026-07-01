package com.ctg.kbFab.retrieval.service;

import com.ctg.kbFab.retrieval.dto.RetrievalRequest;
import com.ctg.kbFab.retrieval.dto.RetrievalResult;
import java.util.List;

/**
 * 检索服务接口
 *
 * @author Developer
 * @since 2026-07-01
 */
public interface RetrievalService {

    /**
     * 执行检索
     */
    List<RetrievalResult> retrieve(RetrievalRequest request);

    /**
     * 简单检索（基于查询字符串）
     */
    List<RetrievalResult> simpleRetrieve(String query, Integer topK);
}
