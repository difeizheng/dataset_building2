package com.ctg.dataFab.qa.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QaTask Entity 单元测试
 *
 * @author Security Officer
 * @since 2026-07-05
 */
class QaTaskTest {

    @Test
    void testGettersAndSetters() {
        QaTask task = new QaTask();
        task.setId(1L);
        task.setDatasetId(100L);
        task.setModality(1);
        task.setBatch("BATCH-001");
        task.setStatus(2);
        task.setMetrics("{\"quality\":85.5}");
        task.setPassed(1);
        task.setGates("[{\"name\":\"completeness\",\"passed\":true}]");
        task.setReviewStage(1);
        task.setReviewComment("通过");
        task.setReviewer("admin");
        task.setReviewTime(LocalDateTime.now());
        task.setCreateBy("admin");
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateBy("admin");
        task.setUpdateTime(LocalDateTime.now());
        task.setDeleted(0);

        assertEquals(1L, task.getId());
        assertEquals(100L, task.getDatasetId());
        assertEquals(1, task.getModality());
        assertEquals("BATCH-001", task.getBatch());
        assertEquals(2, task.getStatus());
        assertEquals("{\"quality\":85.5}", task.getMetrics());
        assertEquals(1, task.getPassed());
        assertEquals("[{\"name\":\"completeness\",\"passed\":true}]", task.getGates());
        assertEquals(1, task.getReviewStage());
        assertEquals("通过", task.getReviewComment());
        assertEquals("admin", task.getReviewer());
        assertNotNull(task.getReviewTime());
        assertEquals("admin", task.getCreateBy());
        assertNotNull(task.getCreateTime());
        assertEquals("admin", task.getUpdateBy());
        assertNotNull(task.getUpdateTime());
        assertEquals(0, task.getDeleted());
    }

    @Test
    void testToString() {
        QaTask task = new QaTask();
        task.setId(1L);
        task.setBatch("BATCH-001");

        String str = task.toString();
        assertNotNull(str);
        assertTrue(str.contains("QaTask"));
    }

    @Test
    void testEqualsAndHashCode() {
        QaTask t1 = new QaTask();
        t1.setId(1L);
        t1.setBatch("BATCH-001");

        QaTask t2 = new QaTask();
        t2.setId(1L);
        t2.setBatch("BATCH-001");

        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
    }
}