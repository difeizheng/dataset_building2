package com.ctg.dataFab.ingest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.common.enums.DataStatus;
import com.ctg.dataFab.common.exception.BusinessException;
import com.ctg.dataFab.ingest.dto.DataSampleCreateRequest;
import com.ctg.dataFab.ingest.entity.DataSample;
import com.ctg.dataFab.ingest.mapper.DataSampleMapper;
import com.ctg.dataFab.ingest.service.DatasetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 数据样本服务单元测试
 *
 * @author Developer
 * @since 2026-07-01
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("数据样本服务测试")
class DataSampleServiceImplTest {

    @Mock
    private DataSampleMapper dataSampleMapper;

    @Mock
    private DatasetService datasetService;

    @InjectMocks
    private DataSampleServiceImpl dataSampleService;

    private DataSampleCreateRequest createRequest;
    private DataSample dataSample;

    @BeforeEach
    void setUp() {
        createRequest = new DataSampleCreateRequest();
        createRequest.setName("测试数据");
        createRequest.setModality(1);
        createRequest.setDataLevel(2);
        createRequest.setFilePath("/data/test.txt");
        createRequest.setFileSize(1024L);
        createRequest.setMimeType("text/plain");

        dataSample = new DataSample();
        dataSample.setId(1L);
        dataSample.setName("测试数据");
        dataSample.setModality(1);
        dataSample.setDataLevel(2);
        dataSample.setStatus(DataStatus.NEW.getCode());
    }

    @Test
    @DisplayName("创建数据样本 - 指定分级")
    void testCreateDataSample_WithLevel() {
        when(dataSampleMapper.insert(any(DataSample.class))).thenAnswer(invocation -> {
            DataSample sample = invocation.getArgument(0);
            sample.setId(1L);
            return 1;
        });

        Long id = dataSampleService.createDataSample(createRequest);

        assertNotNull(id);
        verify(dataSampleMapper).insert(any(DataSample.class));
    }

    @Test
    @DisplayName("创建数据样本 - 自动分级")
    void testCreateDataSample_AutoClassification() {
        createRequest.setDataLevel(null); // 未指定分级

        when(dataSampleMapper.insert(any(DataSample.class))).thenAnswer(invocation -> {
            DataSample sample = invocation.getArgument(0);
            sample.setId(1L);
            return 1;
        });

        Long id = dataSampleService.createDataSample(createRequest);

        assertNotNull(id);
        verify(dataSampleMapper).insert(any(DataSample.class));
    }

    @Test
    @DisplayName("获取数据样本 - 存在")
    void testGetDataSampleById_Exists() {
        when(dataSampleMapper.selectById(1L)).thenReturn(dataSample);

        DataSample result = dataSampleService.getDataSampleById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("测试数据", result.getName());
    }

    @Test
    @DisplayName("获取数据样本 - 不存在")
    void testGetDataSampleById_NotExists() {
        when(dataSampleMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> {
            dataSampleService.getDataSampleById(999L);
        });
    }

    @Test
    @DisplayName("分页查询数据样本")
    void testListDataSamples() {
        Page<DataSample> page = new Page<>(1, 20);
        page.add(dataSample);

        when(dataSampleMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(1);
        pageRequest.setPageSize(20);

        Page<DataSample> result = dataSampleService.listDataSamples(
                null, null, null, null, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("更新数据样本状态")
    void testUpdateDataSampleStatus() {
        when(dataSampleMapper.selectById(1L)).thenReturn(dataSample);
        when(dataSampleMapper.updateById(any(DataSample.class))).thenReturn(1);

        dataSampleService.updateDataSampleStatus(1L, DataStatus.LABELED.getCode());

        verify(dataSampleMapper).updateById(any(DataSample.class));
    }

    @Test
    @DisplayName("删除数据样本")
    void testDeleteDataSample() {
        when(dataSampleMapper.selectById(1L)).thenReturn(dataSample);
        when(dataSampleMapper.deleteById(1L)).thenReturn(1);

        dataSampleService.deleteDataSample(1L);

        verify(dataSampleMapper).deleteById(1L);
    }

    @Test
    @DisplayName("删除数据样本 - 更新数据集统计")
    void testDeleteDataSample_UpdateStats() {
        dataSample.setDatasetId(100L);
        when(dataSampleMapper.selectById(1L)).thenReturn(dataSample);
        when(dataSampleMapper.deleteById(1L)).thenReturn(1);

        dataSampleService.deleteDataSample(1L);

        verify(datasetService).updateDatasetStats(100L);
    }
}
