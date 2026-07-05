package com.ctg.dataFab.access.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AuditLog Entity 单元测试
 *
 * @author Security Officer
 * @since 2026-07-05
 */
class AuditLogTest {

    @Test
    void testGettersAndSetters() {
        AuditLog log = new AuditLog();
        log.setId(1L);
        log.setResourceId(100L);
        log.setResourceType("DATASET");
        log.setAction("DOWNLOAD");
        log.setUserId(200L);
        log.setResult("ALLOWED");
        log.setDescription("下载数据集");
        log.setIpAddress("192.168.1.1");
        log.setCreateTime(LocalDateTime.now());

        assertEquals(1L, log.getId());
        assertEquals(100L, log.getResourceId());
        assertEquals("DATASET", log.getResourceType());
        assertEquals("DOWNLOAD", log.getAction());
        assertEquals(200L, log.getUserId());
        assertEquals("ALLOWED", log.getResult());
        assertEquals("下载数据集", log.getDescription());
        assertEquals("192.168.1.1", log.getIpAddress());
        assertNotNull(log.getCreateTime());
    }

    @Test
    void testToString() {
        AuditLog log = new AuditLog();
        log.setId(1L);
        log.setResourceType("SAMPLE");
        log.setAction("VIEW");

        String str = log.toString();
        assertNotNull(str);
        assertTrue(str.contains("AuditLog"));
    }

    @Test
    void testEqualsAndHashCode() {
        AuditLog l1 = new AuditLog();
        l1.setId(1L);
        l1.setResourceType("DATASET");

        AuditLog l2 = new AuditLog();
        l2.setId(1L);
        l2.setResourceType("DATASET");

        assertEquals(l1, l2);
        assertEquals(l1.hashCode(), l2.hashCode());
    }
}