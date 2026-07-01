package com.ctg.kbFab.ontology.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

/**
 * 本体类创建请求
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
public class CreateOntologyClassRequest {

    @NotBlank(message = "className is required")
    private String className;

    private String parentClassId;

    private String description;

    private List<PropertyDefinition> properties;

    private List<ConstraintDefinition> constraints;

    @Data
    public static class PropertyDefinition {
        private String name;
        private String type;
        private Boolean required;
        private String defaultValue;
    }

    @Data
    public static class ConstraintDefinition {
        private String type;
        private String expression;
        private String message;
    }
}
