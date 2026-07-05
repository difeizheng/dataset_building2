package com.ctg.dataFab.label.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LabelTask Entity 单元测试
 *
 * @author Security Officer
 * @since 2026-07-05
 */
class LabelTaskTest {

    @Test
    void testGettersAndSetters() {
        LabelTask task = new LabelTask();
        task.setId(1L);
        task.setDatasetId(100L);
        task.setTaskName("测试标注任务");
        task.setModality(1);
        task.setLabelType("分类");
        task.setLabelSchema("{\"labels\":[\"A\",\"B\"]}");
        task.setDoubleBlind(1);
        task.setAnnotatorCount(2);
        task.setAnnotatorIds("[\"user1\",\"user2\"]");
        task.setArbitratorId(200L);
        task.setStatus(3);
        task.setTotalSamples(500);
        task.setLabeledCount(300);
        task.setKappaScore(0.85);
        task.setSopDescription("标准操作流程");
        task.setCreateBy("admin");
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateBy("admin");
        task.setUpdateTime(LocalDateTime.now());
        task.setDeleted(0);

        assertEquals(1L, task.getId());
        assertEquals(100L, task.getDatasetId());
        assertEquals("测试标注任务", task.getTaskName());
        assertEquals(1, task.getModality());
        assertEquals("分类", task.getLabelType());
        assertEquals("{\"labels\":[\"A\",\"B\"]}", task.getLabelSchema());
        assertEquals(1, task.getDoubleBlind());
        assertEquals(2, task.getAnnotatorCount());
        assertEquals("[\"user1\",\"user2\"]", task.getAnnotatorIds());
        assertEquals(200L, task.getArbitratorId());
        assertEquals(3, task.getStatus());
        assertEquals(500, task.getTotalSamples());
        assertEquals(300, task.getLabeledCount());
        assertEquals(0.85, task.getKappaScore());
        assertEquals("标准操作流程", task.getSopDescription());
        assertEquals("admin", task.getCreateBy());
        assertNotNull(task.getCreateTime());
        assertEquals("admin", task.getUpdateBy());
        assertNotNull(task.getUpdateTime());
        assertEquals(0, task.getDeleted());
    }

    @Test
    void testToString() {
        LabelTask task = new LabelTask();
        task.setId(1L);
        task.setTaskName("测试任务");
        task.setStatus(2);

        String str = task.toString();
        assertNotNull(str);
        assertTrue(str.contains("LabelTask"));
        assertTrue(str.contains("测试任务"));
    }

    @Test
    void testEqualsAndHashCode() {
        LabelTask task1 = new LabelTask();
        task1.setId(1L);
        task1.setTaskName("任务1");

        LabelTask task2 = new LabelTask();
        task2.setId(1L);
        task2.setTaskName("任务1");

        LabelTask task3 = new LabelTask();
        task3.setId(2L);

        assertEquals(task1, task2);
        assertEquals(task1.hashCode(), task2.hashCode());
        assertNotEquals(task1, task3);
    }
}