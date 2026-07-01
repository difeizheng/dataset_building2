package com.ctg.kbFab.storage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

/**
 * 知识条目创建请求
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
public class CreateKnowledgeRequest {

    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "content is required")
    private String content;

    @NotBlank(message = "knowledgeType is required")
    private String knowledgeType;

    private String sourceDocId;
    private String sourceDocName;
    private String domain;
    private List<String> tags;
}
