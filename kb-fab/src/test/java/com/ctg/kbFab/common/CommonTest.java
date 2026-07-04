package com.ctg.kbFab.common;

import com.ctg.kbFab.common.dto.ApiResponse;
import com.ctg.kbFab.common.dto.PageResponse;
import com.ctg.kbFab.common.enums.KnowledgeStatus;
import com.ctg.kbFab.common.enums.KnowledgeType;
import com.ctg.kbFab.common.enums.ExtractionStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 通用组件测试
 */
class CommonTest {

    @Test
    void testApiResponse_success() {
        ApiResponse<String> response = ApiResponse.success("test data");
        assertTrue(response.getSuccess());
        assertEquals(0, response.getCode());
        assertEquals("ok", response.getMessage());
        assertEquals("test data", response.getData());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void testApiResponse_successNoData() {
        ApiResponse<Void> response = ApiResponse.success();
        assertTrue(response.getSuccess());
        assertNull(response.getData());
    }

    @Test
    void testApiResponse_error() {
        ApiResponse<Void> response = ApiResponse.error(400, "Bad request");
        assertFalse(response.getSuccess());
        assertEquals(400, response.getCode());
        assertEquals("Bad request", response.getMessage());
    }

    @Test
    void testPageResponse() {
        PageResponse<String> response = PageResponse.of(
                List.of("a", "b", "c"), 10L, 1, 3);
        assertEquals(3, response.getRecords().size());
        assertEquals(10L, response.getTotal());
        assertEquals(1, response.getPage());
        assertEquals(3, response.getSize());
        assertEquals(4, response.getPages());
    }

    @Test
    void testKnowledgeType() {
        assertEquals("entity", KnowledgeType.ENTITY.getCode());
        assertEquals("relation", KnowledgeType.RELATION.getCode());
        assertEquals("attribute", KnowledgeType.ATTRIBUTE.getCode());
        assertEquals("event", KnowledgeType.EVENT.getCode());
        assertEquals("concept", KnowledgeType.CONCEPT.getCode());
    }

    @Test
    void testKnowledgeStatus() {
        assertEquals("draft", KnowledgeStatus.DRAFT.getCode());
        assertEquals("published", KnowledgeStatus.PUBLISHED.getCode());
        assertEquals("archived", KnowledgeStatus.ARCHIVED.getCode());
        assertEquals("deprecated", KnowledgeStatus.DEPRECATED.getCode());
    }

    @Test
    void testExtractionStatus() {
        assertEquals("pending", ExtractionStatus.PENDING.getCode());
        assertEquals("processing", ExtractionStatus.PROCESSING.getCode());
        assertEquals("completed", ExtractionStatus.COMPLETED.getCode());
        assertEquals("failed", ExtractionStatus.FAILED.getCode());
    }
}
