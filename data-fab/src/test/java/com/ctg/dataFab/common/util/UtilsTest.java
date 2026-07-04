package com.ctg.dataFab.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 工具类单元测试
 *
 * @author Developer
 * @since 2026-07-05
 */
@DisplayName("工具类测试")
class UtilsTest {

    @Test
    @DisplayName("生成UUID - 非空")
    void testGenerateUUID_NotNull() {
        String uuid = Utils.generateUUID();

        assertNotNull(uuid);
        assertTrue(uuid.length() > 0);
    }

    @Test
    @DisplayName("生成UUID - 不包含连字符")
    void testGenerateUUID_NoHyphens() {
        String uuid = Utils.generateUUID();

        assertFalse(uuid.contains("-"));
    }

    @Test
    @DisplayName("生成UUID - 长度为32")
    void testGenerateUUID_Length() {
        String uuid = Utils.generateUUID();

        assertEquals(32, uuid.length());
    }

    @Test
    @DisplayName("生成UUID - 唯一性")
    void testGenerateUUID_Unique() {
        String uuid1 = Utils.generateUUID();
        String uuid2 = Utils.generateUUID();

        assertNotEquals(uuid1, uuid2);
    }

    @Test
    @DisplayName("生成TraceId - 非空")
    void testGenerateTraceId_NotNull() {
        String traceId = Utils.generateTraceId();

        assertNotNull(traceId);
        assertTrue(traceId.length() > 0);
    }

    @Test
    @DisplayName("生成TraceId - 以trace-开头")
    void testGenerateTraceId_Prefix() {
        String traceId = Utils.generateTraceId();

        assertTrue(traceId.startsWith("trace-"));
    }

    @Test
    @DisplayName("生成TraceId - 长度为38")
    void testGenerateTraceId_Length() {
        String traceId = Utils.generateTraceId();

        assertEquals(38, traceId.length()); // "trace-" (6) + UUID (32)
    }

    @Test
    @DisplayName("生成TraceId - 唯一性")
    void testGenerateTraceId_Unique() {
        String traceId1 = Utils.generateTraceId();
        String traceId2 = Utils.generateTraceId();

        assertNotEquals(traceId1, traceId2);
    }

    @Test
    @DisplayName("多次生成UUID - 全部唯一")
    void testGenerateUUID_MultipleUnique() {
        for (int i = 0; i < 100; i++) {
            String uuid1 = Utils.generateUUID();
            String uuid2 = Utils.generateUUID();
            assertNotEquals(uuid1, uuid2);
        }
    }
}
