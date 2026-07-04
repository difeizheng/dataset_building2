package com.ctg.kbFab.extraction.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 抽取结果
 *
 * @author Developer
 * @since 2026-07-01
 */
@Data
public class ExtractionResult {

    private String taskId;
    private String extractionType;

    /** NER抽取的实体列表 */
    private List<ExtractedEntity> entities;

    /** 关系抽取结果 */
    private List<ExtractedRelation> relations;

    /** 属性抽取结果 */
    private List<ExtractedAttribute> attributes;

    /** 事件抽取结果 */
    private List<ExtractedEvent> events;

    @Data
    public static class ExtractedEntity {
        private String entityId;
        private String text;
        private String type;
        private Double confidence;
        private Integer startOffset;
        private Integer endOffset;
    }

    @Data
    public static class ExtractedRelation {
        private String relationId;
        private String sourceEntityId;
        private String targetEntityId;
        private String relationType;
        private Double confidence;
    }

    @Data
    public static class ExtractedAttribute {
        private String attributeId;
        private String entityId;
        private String attributeName;
        private String attributeValue;
        private Double confidence;
    }

    @Data
    public static class ExtractedEvent {
        private String eventId;
        private String eventType;
        private String trigger;
        private Map<String, String> arguments;
        private Double confidence;
    }
}
