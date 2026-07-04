package com.ctg.dataFab.access.service;

import com.ctg.dataFab.access.entity.AuditLog;
import com.ctg.dataFab.access.mapper.AuditLogMapper;
import com.ctg.dataFab.access.mapper.DataApprovalMapper;
import com.ctg.dataFab.common.enums.DataLevel;
import com.ctg.dataFab.ingest.entity.DataSample;
import com.ctg.dataFab.ingest.mapper.DataSampleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 数据访问控制服务审计功能单元测试
 *
 * @author Developer
 * @since 2026-07-05
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("数据访问控制服务审计测试")
class DataAccessControlServiceAuditTest {

    @Mock
    private DataSampleMapper dataSampleMapper;

    @Mock
    private AuditLogMapper auditLogMapper;

    @Mock
    private DataApprovalMapper dataApprovalMapper;

    @InjectMocks
    private DataAccessControlService dataAccessControlService;

    private DataSample l4Sample;
    private DataSample l3Sample;
    private DataSample l2Sample;

    @BeforeEach
    void setUp() {
        l4Sample = new DataSample();
        l4Sample.setId(100L);
        l4Sample.setDataLevel(DataLevel.L4_CORE.getCode());

        l3Sample = new DataSample();
        l3Sample.setId(200L);
        l3Sample.setDataLevel(DataLevel.L3_SENSITIVE.getCode());

        l2Sample = new DataSample();
        l2Sample.setId(300L);
        l2Sample.setDataLevel(DataLevel.L2_INTERNAL.getCode());
    }

    @Test
    @DisplayName("审计下载访问 - L4阻断")
    void testAuditAccess_DownloadL4Blocked() {
        when(dataSampleMapper.selectById(100L)).thenReturn(l4Sample);

        boolean canDownload = dataAccessControlService.canDownload(100L, 10L);

        assertFalse(canDownload);
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogMapper).insert(captor.capture());
        AuditLog log = captor.getValue();
        assertEquals(100L, log.getResourceId());
        assertEquals("SAMPLE", log.getResourceType());
        assertEquals(10L, log.getUserId());
        assertEquals("DOWNLOAD", log.getAction());
        assertEquals("BLOCKED", log.getResult());
    }

    @Test
    @DisplayName("审计下载访问 - L2允许")
    void testAuditAccess_DownloadL2Allowed() {
        when(dataSampleMapper.selectById(300L)).thenReturn(l2Sample);

        boolean canDownload = dataAccessControlService.canDownload(300L, 10L);

        assertTrue(canDownload);
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogMapper).insert(captor.capture());
        AuditLog log = captor.getValue();
        assertEquals("ALLOWED", log.getResult());
    }

    @Test
    @DisplayName("审计打印访问 - L4阻断")
    void testAuditAccess_PrintL4Blocked() {
        when(dataSampleMapper.selectById(100L)).thenReturn(l4Sample);

        boolean canPrint = dataAccessControlService.canPrint(100L, 10L);

        assertFalse(canPrint);
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogMapper).insert(captor.capture());
        AuditLog log = captor.getValue();
        assertEquals("PRINT", log.getAction());
        assertEquals("BLOCKED", log.getResult());
    }

    @Test
    @DisplayName("审计打印访问 - L2允许")
    void testAuditAccess_PrintL2Allowed() {
        when(dataSampleMapper.selectById(300L)).thenReturn(l2Sample);

        boolean canPrint = dataAccessControlService.canPrint(300L, 10L);

        assertTrue(canPrint);
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogMapper).insert(captor.capture());
        AuditLog log = captor.getValue();
        assertEquals("PRINT", log.getAction());
        assertEquals("ALLOWED", log.getResult());
    }

    @Test
    @DisplayName("审计外发访问 - L4阻断")
    void testAuditAccess_ExportL4Blocked() {
        when(dataSampleMapper.selectById(100L)).thenReturn(l4Sample);

        boolean canExport = dataAccessControlService.canExport(100L, 10L);

        assertFalse(canExport);
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogMapper).insert(captor.capture());
        AuditLog log = captor.getValue();
        assertEquals("EXPORT", log.getAction());
        assertEquals("BLOCKED", log.getResult());
    }

    @Test
    @DisplayName("审计查看访问 - L4允许带水印")
    void testAuditAccess_ViewL4Allowed() {
        when(dataSampleMapper.selectById(100L)).thenReturn(l4Sample);

        boolean canView = dataAccessControlService.canView(100L, 10L);

        assertTrue(canView);
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogMapper).insert(captor.capture());
        AuditLog log = captor.getValue();
        assertEquals("VIEW", log.getAction());
        assertEquals("ALLOWED", log.getResult());
        assertTrue(log.getDescription().contains("水印"));
    }

    @Test
    @DisplayName("检查水印需求 - L4需要")
    void testRequiresWatermark_L4() {
        when(dataSampleMapper.selectById(100L)).thenReturn(l4Sample);

        boolean requires = dataAccessControlService.requiresWatermark(100L);

        assertTrue(requires);
    }

    @Test
    @DisplayName("检查水印需求 - L3需要")
    void testRequiresWatermark_L3() {
        when(dataSampleMapper.selectById(200L)).thenReturn(l3Sample);

        boolean requires = dataAccessControlService.requiresWatermark(200L);

        assertTrue(requires);
    }

    @Test
    @DisplayName("检查水印需求 - L2不需要")
    void testRequiresWatermark_L2() {
        when(dataSampleMapper.selectById(300L)).thenReturn(l2Sample);

        boolean requires = dataAccessControlService.requiresWatermark(300L);

        assertFalse(requires);
    }

    @Test
    @DisplayName("手动审计访问记录")
    void testAuditAccess_Manual() {
        dataAccessControlService.auditAccess(100L, "SAMPLE", 10L, "CUSTOM_ACTION", "CUSTOM_RESULT", "自定义描述");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogMapper).insert(captor.capture());
        AuditLog log = captor.getValue();
        assertEquals(100L, log.getResourceId());
        assertEquals("SAMPLE", log.getResourceType());
        assertEquals(10L, log.getUserId());
        assertEquals("CUSTOM_ACTION", log.getAction());
        assertEquals("CUSTOM_RESULT", log.getResult());
        assertEquals("自定义描述", log.getDescription());
    }
}
