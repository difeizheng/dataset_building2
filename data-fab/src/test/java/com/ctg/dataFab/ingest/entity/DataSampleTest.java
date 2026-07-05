package com.ctg.dataFab.ingest.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DataSample Entity 单元测试
 *
 * @author Security Officer
 * @since 2026-07-05
 */
class DataSampleTest {

    @Test
    void testGettersAndSetters() {
        DataSample sample = new DataSample();
        sample.setId(1L);
        sample.setDatasetId(100L);
        sample.setName("测试样本");
        sample.setModality(1);
        sample.setDataLevel(2);
        sample.setStatus(1);
        sample.setSource("测试数据源");
        sample.setFilePath("/data/samples/1.txt");
        sample.setFileSize(1024L);
        sample.setMimeType("text/plain");
        sample.setFileHash("abc123hash");
        sample.setMetadata("{\"format\":\"text\"}");
        sample.setCreateBy("admin");
        sample.setCreateTime(LocalDateTime.now());
        sample.setUpdateBy("admin");
        sample.setUpdateTime(LocalDateTime.now());
        sample.setDeleted(0);

        assertEquals(1L, sample.getId());
        assertEquals(100L, sample.getDatasetId());
        assertEquals("测试样本", sample.getName());
        assertEquals(1, sample.getModality());
        assertEquals(2, sample.getDataLevel());
        assertEquals(1, sample.getStatus());
        assertEquals("测试数据源", sample.getSource());
        assertEquals("/data/samples/1.txt", sample.getFilePath());
        assertEquals(1024L, sample.getFileSize());
        assertEquals("text/plain", sample.getMimeType());
        assertEquals("abc123hash", sample.getFileHash());
        assertEquals("{\"format\":\"text\"}", sample.getMetadata());
        assertEquals("admin", sample.getCreateBy());
        assertNotNull(sample.getCreateTime());
        assertEquals("admin", sample.getUpdateBy());
        assertNotNull(sample.getUpdateTime());
        assertEquals(0, sample.getDeleted());
    }

    @Test
    void testToString() {
        DataSample sample = new DataSample();
        sample.setId(1L);
        sample.setName("测试内容");

        String str = sample.toString();
        assertNotNull(str);
        assertTrue(str.contains("DataSample"));
    }

    @Test
    void testEqualsAndHashCode() {
        DataSample s1 = new DataSample();
        s1.setId(1L);
        s1.setName("样本1");

        DataSample s2 = new DataSample();
        s2.setId(1L);
        s2.setName("样本1");

        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
    }
}