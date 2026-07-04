package com.ctg.kbFab.ontology.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 本体关系创建请求
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
public class CreateOntologyRelationRequest {

    @NotBlank(message = "relationName is required")
    private String relationName;

    @NotBlank(message = "sourceClassId is required")
    private String sourceClassId;

    @NotBlank(message = "targetClassId is required")
    private String targetClassId;

    private String description;

    private String cardinality = "1:N";
}
