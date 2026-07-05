package com.ctg.dataFab.access.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DataApproval Entity 单元测试
 *
 * @author Security Officer
 * @since 2026-07-05
 */
class DataApprovalTest {

    @Test
    void testGettersAndSetters() {
        DataApproval approval = new DataApproval();
        approval.setId(1L);
        approval.setApprovalNo("APR-20240101-001");
        approval.setResourceId(100L);
        approval.setResourceType("DATASET");
        approval.setAction("DOWNLOAD");
        approval.setApplicantId(200L);
        approval.setApproverId(300L);
        approval.setStatus(1);
        approval.setComment("同意");
        approval.setApproveTime(LocalDateTime.now());
        approval.setExpireTime(LocalDateTime.now().plusDays(7));
        approval.setCreateTime(LocalDateTime.now());

        assertEquals(1L, approval.getId());
        assertEquals("APR-20240101-001", approval.getApprovalNo());
        assertEquals(100L, approval.getResourceId());
        assertEquals("DATASET", approval.getResourceType());
        assertEquals("DOWNLOAD", approval.getAction());
        assertEquals(200L, approval.getApplicantId());
        assertEquals(300L, approval.getApproverId());
        assertEquals(1, approval.getStatus());
        assertEquals("同意", approval.getComment());
        assertNotNull(approval.getApproveTime());
        assertNotNull(approval.getExpireTime());
        assertNotNull(approval.getCreateTime());
    }

    @Test
    void testToString() {
        DataApproval a = new DataApproval();
        a.setId(1L);
        a.setApprovalNo("APR-001");

        String str = a.toString();
        assertNotNull(str);
        assertTrue(str.contains("DataApproval"));
    }

    @Test
    void testEqualsAndHashCode() {
        DataApproval a1 = new DataApproval();
        a1.setId(1L);
        a1.setApprovalNo("APR-001");

        DataApproval a2 = new DataApproval();
        a2.setId(1L);
        a2.setApprovalNo("APR-001");

        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());
    }
}