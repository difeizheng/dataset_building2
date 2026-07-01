package com.ctg.kbFab.extraction.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 抽取请求
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
public class ExtractionRequest {

    @NotBlank(message = "taskName is required")
    private String taskName;

    @NotBlank(message = "inputText is required")
    private String inputText;

    /** NER / RELATION / ATTRIBUTE / EVENT */
    @NotBlank(message = "extractionType is required")
    private String extractionType;
}
