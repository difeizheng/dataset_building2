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

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 数据样本服务边界条件单元测试
 *
 * @author Developer
 * @since 2026-07-05
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("数据样本服务边界测试")
class DataSampleServiceImplBoundaryTest {

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
    @DisplayName("创建数据样本 - 零大小文件")
    void testCreateDataSample_ZeroSize() {
        createRequest.setFileSize(0L);

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
    @DisplayName("创建数据样本 - 大文件")
    void testCreateDataSample_LargeFile() {
        createRequest.setFileSize(1024L * 1024 * 1024 * 10); // 10GB

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
    @DisplayName("创建数据样本 - 空文件路径")
    void testCreateDataSample_EmptyPath() {
        createRequest.setFilePath("");

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
    @DisplayName("更新状态 - 相同状态")
    void testUpdateDataSampleStatus_SameStatus() {
        dataSample.setStatus(DataStatus.NEW.getCode());
        when(dataSampleMapper.selectById(1L)).thenReturn(dataSample);
        when(dataSampleMapper.updateById(any(DataSample.class))).thenReturn(1);

        dataSampleService.updateDataSampleStatus(1L, DataStatus.NEW.getCode());

        verify(dataSampleMapper).updateById(any(DataSample.class));
    }

    @Test
    @DisplayName("更新状态 - 到已清洗状态")
    void testUpdateDataSampleStatus_ToClean() {
        when(dataSampleMapper.selectById(1L)).thenReturn(dataSample);
        when(dataSampleMapper.updateById(any(DataSample.class))).thenReturn(1);

        dataSampleService.updateDataSampleStatus(1L, DataStatus.CLEAN.getCode());

        verify(dataSampleMapper).updateById(any(DataSample.class));
    }

    @Test
    @DisplayName("更新状态 - 到已标注状态")
    void testUpdateDataSampleStatus_ToLabeled() {
        when(dataSampleMapper.selectById(1L)).thenReturn(dataSample);
        when(dataSampleMapper.updateById(any(DataSample.class))).thenReturn(1);

        dataSampleService.updateDataSampleStatus(1L, DataStatus.LABELED.getCode());

        verify(dataSampleMapper).updateById(any(DataSample.class));
    }

    @Test
    @DisplayName("分页查询 - 空结果")
    void testListDataSamples_EmptyResult() {
        Page<DataSample> page = new Page<>(1, 20);
        page.setRecords(Collections.emptyList());

        when(dataSampleMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(1);
        pageRequest.setPageSize(20);

        Page<DataSample> result = dataSampleService.listDataSamples(
                null, null, null, null, pageRequest);

        assertNotNull(result);
        assertEquals(0, result.getRecords().size());
    }

    @Test
    @DisplayName("分页查询 - 按数据集过滤")
    void testListDataSamples_FilterByDataset() {
        Page<DataSample> page = new Page<>(1, 20);
        page.setRecords(Collections.singletonList(dataSample));

        when(dataSampleMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(1);
        pageRequest.setPageSize(20);

        Page<DataSample> result = dataSampleService.listDataSamples(
                1L, null, null, null, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("分页查询 - 按状态过滤")
    void testListDataSamples_FilterByStatus() {
        Page<DataSample> page = new Page<>(1, 20);
        page.setRecords(Collections.singletonList(dataSample));

        when(dataSampleMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(1);
        pageRequest.setPageSize(20);

        Page<DataSample> result = dataSampleService.listDataSamples(
                null, DataStatus.NEW.getCode(), null, null, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("删除样本 - 无数据集关联")
    void testDeleteDataSample_NoDataset() {
        dataSample.setDatasetId(null);
        when(dataSampleMapper.selectById(1L)).thenReturn(dataSample);
        when(dataSampleMapper.deleteById(1L)).thenReturn(1);

        dataSampleService.deleteDataSample(1L);

        verify(dataSampleMapper).deleteById(1L);
        verify(datasetService, never()).updateDatasetStats(anyLong());
    }

    @Test
    @DisplayName("删除样本 - 有数据集关联")
    void testDeleteDataSample_WithDataset() {
        dataSample.setDatasetId(100L);
        when(dataSampleMapper.selectById(1L)).thenReturn(dataSample);
        when(dataSampleMapper.deleteById(1L)).thenReturn(1);

        dataSampleService.deleteDataSample(1L);

        verify(dataSampleMapper).deleteById(1L);
        verify(datasetService).updateDatasetStats(100L);
    }

    @Test
    @DisplayName("获取样本 - 不存在")
    void testGetDataSampleById_NotExists() {
        when(dataSampleMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> {
            dataSampleService.getDataSampleById(999L);
        });
    }
}
