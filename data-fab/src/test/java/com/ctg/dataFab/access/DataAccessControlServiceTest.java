package com.ctg.dataFab.access;

import com.ctg.dataFab.access.service.DataAccessControlService;
import com.ctg.dataFab.common.enums.DataLevel;
import com.ctg.dataFab.ingest.entity.DataSample;
import com.ctg.dataFab.ingest.mapper.DataSampleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * 数据访问控制服务单元测试
 *
 * @author Developer
 * @since 2026-07-01
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("数据访问控制服务测试")
class DataAccessControlServiceTest {

    @Mock
    private DataSampleMapper dataSampleMapper;

    @InjectMocks
    private DataAccessControlService accessControlService;

    private DataSample l4Sample;
    private DataSample l3Sample;
    private DataSample l2Sample;
    private DataSample l1Sample;

    @BeforeEach
    void setUp() {
        l4Sample = new DataSample();
        l4Sample.setId(1L);
        l4Sample.setDataLevel(DataLevel.L4_CORE.getCode());

        l3Sample = new DataSample();
        l3Sample.setId(2L);
        l3Sample.setDataLevel(DataLevel.L3_SENSITIVE.getCode());

        l2Sample = new DataSample();
        l2Sample.setId(3L);
        l2Sample.setDataLevel(DataLevel.L2_INTERNAL.getCode());

        l1Sample = new DataSample();
        l1Sample.setId(4L);
        l1Sample.setDataLevel(DataLevel.L1_PUBLIC.getCode());
    }

    @Test
    @DisplayName("L4核心数据禁止下载")
    void testCanDownload_L4Core_Blocked() {
        when(dataSampleMapper.selectById(1L)).thenReturn(l4Sample);

        boolean result = accessControlService.canDownload(1L, 100L);

        assertFalse(result, "L4核心数据应该禁止下载");
    }

    @Test
    @DisplayName("L3敏感数据下载需要审批")
    void testCanDownload_L3Sensitive_NeedsApproval() {
        when(dataSampleMapper.selectById(2L)).thenReturn(l3Sample);

        boolean result = accessControlService.canDownload(2L, 100L);

        assertFalse(result, "L3敏感数据下载需要审批，默认拒绝");
    }

    @Test
    @DisplayName("L2内部数据允许下载")
    void testCanDownload_L2Internal_Allowed() {
        when(dataSampleMapper.selectById(3L)).thenReturn(l2Sample);

        boolean result = accessControlService.canDownload(3L, 100L);

        assertTrue(result, "L2内部数据应该允许下载");
    }

    @Test
    @DisplayName("L1公开数据允许下载")
    void testCanDownload_L1Public_Allowed() {
        when(dataSampleMapper.selectById(4L)).thenReturn(l1Sample);

        boolean result = accessControlService.canDownload(4L, 100L);

        assertTrue(result, "L1公开数据应该允许下载");
    }

    @Test
    @DisplayName("L4核心数据禁止打印")
    void testCanPrint_L4Core_Blocked() {
        when(dataSampleMapper.selectById(1L)).thenReturn(l4Sample);

        boolean result = accessControlService.canPrint(1L, 100L);

        assertFalse(result, "L4核心数据应该禁止打印");
    }

    @Test
    @DisplayName("L4核心数据禁止外发")
    void testCanExport_L4Core_Blocked() {
        when(dataSampleMapper.selectById(1L)).thenReturn(l4Sample);

        boolean result = accessControlService.canExport(1L, 100L);

        assertFalse(result, "L4核心数据应该禁止外发");
    }

    @Test
    @DisplayName("L4核心数据允许在线查看")
    void testCanView_L4Core_Allowed() {
        when(dataSampleMapper.selectById(1L)).thenReturn(l4Sample);

        boolean result = accessControlService.canView(1L, 100L);

        assertTrue(result, "L4核心数据应该允许在线查看");
    }

    @Test
    @DisplayName("L4核心数据需要水印")
    void testRequiresWatermark_L4Core() {
        when(dataSampleMapper.selectById(1L)).thenReturn(l4Sample);

        boolean result = accessControlService.requiresWatermark(1L);

        assertTrue(result, "L4核心数据需要水印");
    }

    @Test
    @DisplayName("L3敏感数据需要水印")
    void testRequiresWatermark_L3Sensitive() {
        when(dataSampleMapper.selectById(2L)).thenReturn(l3Sample);

        boolean result = accessControlService.requiresWatermark(2L);

        assertTrue(result, "L3敏感数据需要水印");
    }

    @Test
    @DisplayName("L2内部数据不需要水印")
    void testRequiresWatermark_L2Internal() {
        when(dataSampleMapper.selectById(3L)).thenReturn(l2Sample);

        boolean result = accessControlService.requiresWatermark(3L);

        assertFalse(result, "L2内部数据不需要水印");
    }
}
