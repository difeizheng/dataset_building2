package com.ctg.dataFab.delivery.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DeliveryRecord Entity 单元测试
 *
 * @author Security Officer
 * @since 2026-07-05
 */
class DeliveryRecordTest {

    @Test
    void testGettersAndSetters() {
        DeliveryRecord record = new DeliveryRecord();
        record.setId(1L);
        record.setDatasetId(100L);
        record.setVersion("1.0.0");
        record.setDatasetUri("https://example.com/dataset/1");
        record.setLicense("MIT");
        record.setLineage("{\"parent\":\"dataset-0\"}");
        record.setAccessPolicy("{\"public\":true}");
        record.setFairMetadata("{\"conformsTo\":\"FAIR\"}");
        record.setCroissantMetadata("{\"@type\":\"Dataset\"}");
        record.setStatus(1);
        record.setDownloadCount(50);
        record.setCreateBy("admin");
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateBy("admin");
        record.setUpdateTime(LocalDateTime.now());
        record.setDeleted(0);

        assertEquals(1L, record.getId());
        assertEquals(100L, record.getDatasetId());
        assertEquals("1.0.0", record.getVersion());
        assertEquals("https://example.com/dataset/1", record.getDatasetUri());
        assertEquals("MIT", record.getLicense());
        assertEquals("{\"parent\":\"dataset-0\"}", record.getLineage());
        assertEquals("{\"public\":true}", record.getAccessPolicy());
        assertEquals("{\"conformsTo\":\"FAIR\"}", record.getFairMetadata());
        assertEquals("{\"@type\":\"Dataset\"}", record.getCroissantMetadata());
        assertEquals(1, record.getStatus());
        assertEquals(50, record.getDownloadCount());
        assertEquals("admin", record.getCreateBy());
        assertNotNull(record.getCreateTime());
        assertEquals("admin", record.getUpdateBy());
        assertNotNull(record.getUpdateTime());
        assertEquals(0, record.getDeleted());
    }

    @Test
    void testToString() {
        DeliveryRecord record = new DeliveryRecord();
        record.setId(1L);
        record.setVersion("1.0.0");

        String str = record.toString();
        assertNotNull(str);
        assertTrue(str.contains("DeliveryRecord"));
    }

    @Test
    void testEqualsAndHashCode() {
        DeliveryRecord r1 = new DeliveryRecord();
        r1.setId(1L);
        r1.setDatasetId(100L);

        DeliveryRecord r2 = new DeliveryRecord();
        r2.setId(1L);
        r2.setDatasetId(100L);

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }
}