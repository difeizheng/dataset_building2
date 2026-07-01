package com.ctg.kbFab.retrieval.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 检索请求
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
public class RetrievalRequest {

    @NotBlank(message = "query is required")
    private String query;

    /** 检索模式：VECTOR / GRAPH / FULLTEXT / HYBRID */
    private String mode = "HYBRID";

    /** 返回结果数量 */
    private Integer topK = 10;

    /** 领域过滤 */
    private String domain;

    /** 知识类型过滤 */
    private List<String> knowledgeTypes;

    /** 元数据过滤条件 */
    private Map<String, Object> filters;

    /** 是否启用重排序 */
    private Boolean rerank = true;

    /** 最小相关度阈值 */
    private Double minScore = 0.5;
}
