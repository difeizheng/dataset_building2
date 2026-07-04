package com.ctg.kbFab.retrieval.dto;

import lombok.Data;
import java.util.Map;

/**
 * 检索结果
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
public class RetrievalResult {

    /** 知识条目ID */
    private String knowledgeId;

    /** 标题 */
    private String title;

    /** 内容片段 */
    private String content;

    /** 知识类型 */
    private String knowledgeType;

    /** 领域 */
    private String domain;

    /** 综合得分 */
    private Double score;

    /** 来源：VECTOR / GRAPH / FULLTEXT */
    private String source;

    /** 元数据 */
    private Map<String, Object> metadata;

    /** 向量检索得分 */
    private Double vectorScore;

    /** 图谱检索得分 */
    private Double graphScore;

    /** 全文检索得分 */
    private Double fulltextScore;

    /** 重排后得分 */
    private Double rerankScore;
}
