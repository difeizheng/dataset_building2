package com.ctg.dataFab.etl.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EtlTask Entity 单元测试
 *
 * @author Security Officer
 * @since 2026-07-05
 */
class EtlTaskTest {

    @Test
    void testGettersAndSetters() {
        EtlTask task = new EtlTask();
        task.setId(1L);
        task.setDatasetId(100L);
        task.setTaskName("ETL任务");
        task.setStatus(2);
        task.setProcessedCount(5000);
        task.setSuccessCount(4900);
        task.setFailedCount(100);
        task.setStartTime(LocalDateTime.now());
        task.setEndTime(LocalDateTime.now());
        task.setErrorMessage("无错误");
        task.setCreateBy("admin");
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateBy("admin");
        task.setUpdateTime(LocalDateTime.now());
        task.setDeleted(0);

        assertEquals(1L, task.getId());
        assertEquals(100L, task.getDatasetId());
        assertEquals("ETL任务", task.getTaskName());
        assertEquals(2, task.getStatus());
        assertEquals(5000, task.getProcessedCount());
        assertEquals(4900, task.getSuccessCount());
        assertEquals(100, task.getFailedCount());
        assertNotNull(task.getStartTime());
        assertNotNull(task.getEndTime());
        assertEquals("无错误", task.getErrorMessage());
        assertEquals("admin", task.getCreateBy());
        assertNotNull(task.getCreateTime());
        assertEquals("admin", task.getUpdateBy());
        assertNotNull(task.getUpdateTime());
        assertEquals(0, task.getDeleted());
    }

    @Test
    void testToString() {
        EtlTask task = new EtlTask();
        task.setId(1L);
        task.setTaskName("ETL任务");

        String str = task.toString();
        assertNotNull(str);
        assertTrue(str.contains("EtlTask"));
    }

    @Test
    void testEqualsAndHashCode() {
        EtlTask t1 = new EtlTask();
        t1.setId(1L);
        t1.setTaskName("任务1");

        EtlTask t2 = new EtlTask();
        t2.setId(1L);
        t2.setTaskName("任务1");

        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
    }
}