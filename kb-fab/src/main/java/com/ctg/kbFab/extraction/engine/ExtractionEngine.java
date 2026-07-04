package com.ctg.kbFab.extraction.engine;

import com.ctg.kbFab.extraction.dto.ExtractionResult;
import com.ctg.kbFab.extraction.dto.ExtractionResult.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 知识抽取引擎
 * 实现NER、关系抽取、属性抽取、事件抽取
 *
 * @author Developer
 * @since 2026-07-01
 */
@Slf4j
@Component
public class ExtractionEngine {

    /**
     * 命名实体识别(NER)
     */
    public List<ExtractedEntity> extractEntities(String text) {
        List<ExtractedEntity> entities = new ArrayList<>();

        // 基于规则的人名识别
        Pattern personPattern = Pattern.compile("([\\u4e00-\\u9fa5]{2,4})(?:先生|女士|博士|教授)");
        Matcher personMatcher = personPattern.matcher(text);
        while (personMatcher.find()) {
            ExtractedEntity entity = new ExtractedEntity();
            entity.setEntityId(generateId());
            entity.setText(personMatcher.group(1));
            entity.setType("PERSON");
            entity.setConfidence(0.85);
            entity.setStartOffset(personMatcher.start());
            entity.setEndOffset(personMatcher.end());
            entities.add(entity);
        }

        // 组织机构识别
        Pattern orgPattern = Pattern.compile("([\\u4e00-\\u9fa5]{2,20})(?:公司|集团|研究院|大学)");
        Matcher orgMatcher = orgPattern.matcher(text);
        while (orgMatcher.find()) {
            ExtractedEntity entity = new ExtractedEntity();
            entity.setEntityId(generateId());
            entity.setText(orgMatcher.group(1));
            entity.setType("ORGANIZATION");
            entity.setConfidence(0.88);
            entity.setStartOffset(orgMatcher.start());
            entity.setEndOffset(orgMatcher.end());
            entities.add(entity);
        }

        // 地点识别
        Pattern locationPattern = Pattern.compile("([\\u4e00-\\u9fa5]{2,10})(?:省|市|区|县|镇)");
        Matcher locationMatcher = locationPattern.matcher(text);
        while (locationMatcher.find()) {
            ExtractedEntity entity = new ExtractedEntity();
            entity.setEntityId(generateId());
            entity.setText(locationMatcher.group(1));
            entity.setType("LOCATION");
            entity.setConfidence(0.82);
            entity.setStartOffset(locationMatcher.start());
            entity.setEndOffset(locationMatcher.end());
            entities.add(entity);
        }

        log.debug("Extracted {} entities from text", entities.size());
        return entities;
    }

    /**
     * 关系抽取
     */
    public List<ExtractedRelation> extractRelations(String text, List<ExtractedEntity> entities) {
        List<ExtractedRelation> relations = new ArrayList<>();

        // 基于模式的关系识别
        for (int i = 0; i < entities.size(); i++) {
            for (int j = i + 1; j < entities.size(); j++) {
                ExtractedEntity e1 = entities.get(i);
                ExtractedEntity e2 = entities.get(j);

                // 检查两个实体之间是否存在关系模式
                String pattern = text.substring(
                    Math.min(e1.getEndOffset(), e2.getEndOffset()),
                    Math.max(e1.getStartOffset(), e2.getStartOffset())
                );

                if (pattern.contains("属于") || pattern.contains("位于")) {
                    ExtractedRelation relation = new ExtractedRelation();
                    relation.setRelationId(generateId());
                    relation.setSourceEntityId(e1.getEntityId());
                    relation.setTargetEntityId(e2.getEntityId());
                    relation.setRelationType("LOCATED_IN");
                    relation.setConfidence(0.75);
                    relations.add(relation);
                }
            }
        }

        log.debug("Extracted {} relations from text", relations.size());
        return relations;
    }

    /**
     * 属性抽取
     */
    public List<ExtractedAttribute> extractAttributes(String text, List<ExtractedEntity> entities) {
        List<ExtractedAttribute> attributes = new ArrayList<>();

        // 基于模式的属性识别
        Pattern attrPattern = Pattern.compile("([\\u4e00-\\u9fa5]{2,10})是([\\u4e00-\\u9fa50-9]+)");
        Matcher matcher = attrPattern.matcher(text);

        while (matcher.find()) {
            String attrName = matcher.group(1);
            String attrValue = matcher.group(2);

            // 查找关联的实体
            for (ExtractedEntity entity : entities) {
                if (text.contains(entity.getText() + "的" + attrName)) {
                    ExtractedAttribute attr = new ExtractedAttribute();
                    attr.setAttributeId(generateId());
                    attr.setEntityId(entity.getEntityId());
                    attr.setAttributeName(attrName);
                    attr.setAttributeValue(attrValue);
                    attr.setConfidence(0.80);
                    attributes.add(attr);
                }
            }
        }

        log.debug("Extracted {} attributes from text", attributes.size());
        return attributes;
    }

    /**
     * 事件抽取
     */
    public List<ExtractedEvent> extractEvents(String text) {
        List<ExtractedEvent> events = new ArrayList<>();

        // 基于触发词的事件识别
        String[] eventTriggers = {"发布", "签署", "启动", "完成", "召开", "举行"};

        for (String trigger : eventTriggers) {
            int index = text.indexOf(trigger);
            if (index >= 0) {
                ExtractedEvent event = new ExtractedEvent();
                event.setEventId(generateId());
                event.setEventType(determineEventType(trigger));
                event.setTrigger(trigger);
                event.setConfidence(0.78);
                events.add(event);
            }
        }

        log.debug("Extracted {} events from text", events.size());
        return events;
    }

    private String determineEventType(String trigger) {
        return switch (trigger) {
            case "发布" -> "PUBLISH";
            case "签署" -> "SIGN";
            case "启动" -> "START";
            case "完成" -> "COMPLETE";
            case "召开", "举行" -> "MEETING";
            default -> "OTHER";
        };
    }

    private String generateId() {
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
