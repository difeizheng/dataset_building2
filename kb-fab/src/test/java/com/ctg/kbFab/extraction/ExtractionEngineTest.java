package com.ctg.kbFab.extraction;

import com.ctg.kbFab.extraction.dto.ExtractionResult;
import com.ctg.kbFab.extraction.engine.ExtractionEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 知识抽取引擎测试
 */
class ExtractionEngineTest {

    private ExtractionEngine engine;

    @BeforeEach
    void setUp() {
        engine = new ExtractionEngine();
    }

    @Test
    void testExtractEntities_person() {
        String text = "张三先生在北京大学发表了演讲";
        List<ExtractionResult.ExtractedEntity> entities = engine.extractEntities(text);
        assertFalse(entities.isEmpty());
        assertTrue(entities.stream().anyMatch(e -> e.getType().equals("PERSON")));
    }

    @Test
    void testExtractEntities_organization() {
        String text = "三峡集团与清华大学签署合作协议";
        List<ExtractionResult.ExtractedEntity> entities = engine.extractEntities(text);
        assertFalse(entities.isEmpty());
        assertTrue(entities.stream().anyMatch(e -> e.getType().equals("ORGANIZATION")));
    }

    @Test
    void testExtractEntities_location() {
        String text = "项目位于湖北省宜昌市";
        List<ExtractionResult.ExtractedEntity> entities = engine.extractEntities(text);
        assertFalse(entities.isEmpty());
        assertTrue(entities.stream().anyMatch(e -> e.getType().equals("LOCATION")));
    }

    @Test
    void testExtractEntities_empty() {
        List<ExtractionResult.ExtractedEntity> entities = engine.extractEntities("");
        assertTrue(entities.isEmpty());
    }

    @Test
    void testExtractRelations() {
        String text = "三峡集团位于湖北省";
        var entities = engine.extractEntities(text);
        var relations = engine.extractRelations(text, entities);
        // 关系抽取结果取决于实体识别
        assertNotNull(relations);
    }

    @Test
    void testExtractAttributes() {
        String text = "张三的颜色是红色";
        var entities = engine.extractEntities(text);
        var attributes = engine.extractAttributes(text, entities);
        assertNotNull(attributes);
    }

    @Test
    void testExtractEvents() {
        String text = "三峡集团发布了新产品";
        var events = engine.extractEvents(text);
        assertFalse(events.isEmpty());
        assertEquals("PUBLISH", events.get(0).getEventType());
    }

    @Test
    void testExtractEvents_multiple() {
        String text = "公司召开了会议并签署了协议";
        var events = engine.extractEvents(text);
        assertTrue(events.size() >= 2);
    }

    @Test
    void testExtractEvents_empty() {
        var events = engine.extractEvents("今天天气很好");
        assertTrue(events.isEmpty());
    }
}
