package com.ctg.dataFab.label.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LabelRecord Entity 单元测试
 *
 * @author Security Officer
 * @since 2026-07-05
 */
class LabelRecordTest {

    @Test
    void testGettersAndSetters() {
        LabelRecord record = new LabelRecord();
        record.setId(1L);
        record.setTaskId(100L);
        record.setSampleId(200L);
        record.setAnnotatorId(300L);
        record.setAnnotations("{\"label\":\"A\"}");
        record.setStatus(1);
        record.setDurationSeconds(120L);
        record.setSubmitTime(LocalDateTime.now());
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());

        assertEquals(1L, record.getId());
        assertEquals(100L, record.getTaskId());
        assertEquals(200L, record.getSampleId());
        assertEquals(300L, record.getAnnotatorId());
        assertEquals("{\"label\":\"A\"}", record.getAnnotations());
        assertEquals(1, record.getStatus());
        assertEquals(120L, record.getDurationSeconds());
        assertNotNull(record.getSubmitTime());
        assertNotNull(record.getCreateTime());
        assertNotNull(record.getUpdateTime());
    }

    @Test
    void testToString() {
        LabelRecord r = new LabelRecord();
        r.setId(1L);
        r.setAnnotations("{}");

        String str = r.toString();
        assertNotNull(str);
        assertTrue(str.contains("LabelRecord"));
    }

    @Test
    void testEqualsAndHashCode() {
        LabelRecord r1 = new LabelRecord();
        r1.setId(1L);
        r1.setTaskId(100L);

        LabelRecord r2 = new LabelRecord();
        r2.setId(1L);
        r2.setTaskId(100L);

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }
}