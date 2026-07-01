package com.ctg.kbFab.access;

import com.ctg.kbFab.access.entity.KnowledgeAccess;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 访问权限实体测试
 */
class AccessEntityTest {

    @Test
    void testKnowledgeAccess() {
        KnowledgeAccess access = new KnowledgeAccess();
        access.setId("acc-001");
        access.setKnowledgeId("k-001");
        access.setPrincipalId("user-001");
        access.setPrincipalType("USER");
        access.setAccessLevel("WRITE");
        access.setAllowed(true);
        access.setExpireTime(LocalDateTime.now().plusDays(30));

        assertEquals("acc-001", access.getId());
        assertEquals("k-001", access.getKnowledgeId());
        assertEquals("user-001", access.getPrincipalId());
        assertEquals("USER", access.getPrincipalType());
        assertEquals("WRITE", access.getAccessLevel());
        assertTrue(access.getAllowed());
        assertNotNull(access.getExpireTime());
    }

    @Test
    void testKnowledgeAccess_expired() {
        KnowledgeAccess access = new KnowledgeAccess();
        access.setExpireTime(LocalDateTime.now().minusDays(1));
        assertTrue(access.getExpireTime().isBefore(LocalDateTime.now()));
    }
}
