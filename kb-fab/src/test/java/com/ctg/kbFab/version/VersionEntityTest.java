package com.ctg.kbFab.version;

import com.ctg.kbFab.version.entity.KnowledgeVersion;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 版本管理实体测试
 */
class VersionEntityTest {

    @Test
    void testKnowledgeVersion() {
        KnowledgeVersion version = new KnowledgeVersion();
        version.setId("v-001");
        version.setKnowledgeId("k-001");
        version.setVersionNumber(1);
        version.setTitle("初始版本");
        version.setContentSnapshot("测试内容");
        version.setChangeNote("初始创建");
        version.setOperator("admin");
        version.setOperationType("CREATE");
        version.setCreateTime(LocalDateTime.now());

        assertEquals("v-001", version.getId());
        assertEquals("k-001", version.getKnowledgeId());
        assertEquals(1, version.getVersionNumber());
        assertEquals("CREATE", version.getOperationType());
    }
}
