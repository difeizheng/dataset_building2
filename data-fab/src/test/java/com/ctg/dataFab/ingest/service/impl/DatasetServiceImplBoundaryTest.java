package com.ctg.dataFab.ingest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.common.enums.DataStatus;
import com.ctg.dataFab.common.exception.BusinessException;
import com.ctg.dataFab.ingest.dto.DatasetCreateRequest;
import com.ctg.dataFab.ingest.entity.Dataset;
import com.ctg.dataFab.ingest.mapper.DataSampleMapper;
import com.ctg.dataFab.ingest.mapper.DatasetMapper;
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
 * 数据集服务边界条件单元测试
 *
 * @author Developer
 * @since 2026-07-05
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("数据集服务边界测试")
class DatasetServiceImplBoundaryTest {

    @Mock
    private DatasetMapper datasetMapper;

    @Mock
    private DataSampleMapper dataSampleMapper;

    @InjectMocks
    private DatasetServiceImpl datasetService;

    private DatasetCreateRequest createRequest;
    private Dataset dataset;

    @BeforeEach
    void setUp() {
        createRequest = new DatasetCreateRequest();
        createRequest.setName("测试数据集");
        createRequest.setDescription("测试描述");
        createRequest.setModality(1);
        createRequest.setDataLevel(2);
        createRequest.setVersion("1.0.0");

        dataset = new Dataset();
        dataset.setId(1L);
        dataset.setName("测试数据集");
        dataset.setModality(1);
        dataset.setDataLevel(2);
        dataset.setStatus(DataStatus.NEW.getCode());
        dataset.setSampleCount(0);
        dataset.setTotalSize(0L);
    }

    @Test
    @DisplayName("创建数据集 - 空描述")
    void testCreateDataset_EmptyDescription() {
        createRequest.setDescription("");

        when(datasetMapper.insert(any(Dataset.class))).thenAnswer(invocation -> {
            Dataset ds = invocation.getArgument(0);
            ds.setId(1L);
            return 1;
        });

        Long id = datasetService.createDataset(createRequest);

        assertNotNull(id);
        verify(datasetMapper).insert(any(Dataset.class));
    }

    @Test
    @DisplayName("创建数据集 - null描述")
    void testCreateDataset_NullDescription() {
        createRequest.setDescription(null);

        when(datasetMapper.insert(any(Dataset.class))).thenAnswer(invocation -> {
            Dataset ds = invocation.getArgument(0);
            ds.setId(1L);
            return 1;
        });

        Long id = datasetService.createDataset(createRequest);

        assertNotNull(id);
        verify(datasetMapper).insert(any(Dataset.class));
    }

    @Test
    @DisplayName("创建数据集 - 未指定分级")
    void testCreateDataset_NoDataLevel() {
        createRequest.setDataLevel(null);

        when(datasetMapper.insert(any(Dataset.class))).thenAnswer(invocation -> {
            Dataset ds = invocation.getArgument(0);
            ds.setId(1L);
            return 1;
        });

        Long id = datasetService.createDataset(createRequest);

        assertNotNull(id);
        verify(datasetMapper).insert(any(Dataset.class));
    }

    @Test
    @DisplayName("更新数据集统计 - 无样本")
    void testUpdateDatasetStats_NoSamples() {
        when(datasetMapper.selectById(1L)).thenReturn(dataset);
        when(dataSampleMapper.selectCount(any())).thenReturn(0L);
        when(dataSampleMapper.sumFileSizeByDatasetId(1L)).thenReturn(0L);
        when(datasetMapper.updateById(any(Dataset.class))).thenReturn(1);

        datasetService.updateDatasetStats(1L);

        assertEquals(Integer.valueOf(0), dataset.getSampleCount());
        assertEquals(Long.valueOf(0L), dataset.getTotalSize());
        verify(datasetMapper).updateById(any(Dataset.class));
    }

    @Test
    @DisplayName("更新数据集统计 - 大量样本")
    void testUpdateDatasetStats_ManySamples() {
        when(datasetMapper.selectById(1L)).thenReturn(dataset);
        when(dataSampleMapper.selectCount(any())).thenReturn(1000000L);
        when(dataSampleMapper.sumFileSizeByDatasetId(1L)).thenReturn(1024L * 1024 * 1024); // 1GB
        when(datasetMapper.updateById(any(Dataset.class))).thenReturn(1);

        datasetService.updateDatasetStats(1L);

        assertEquals(Integer.valueOf(1000000), dataset.getSampleCount());
        assertEquals(Long.valueOf(1024L * 1024 * 1024), dataset.getTotalSize());
        verify(datasetMapper).updateById(any(Dataset.class));
    }

    @Test
    @DisplayName("分页查询 - 空结果")
    void testListDatasets_EmptyResult() {
        Page<Dataset> page = new Page<>(1, 20);
        page.setRecords(Collections.emptyList());

        when(datasetMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(1);
        pageRequest.setPageSize(20);

        Page<Dataset> result = datasetService.listDatasets(null, null, null, pageRequest);

        assertNotNull(result);
        assertEquals(0, result.getRecords().size());
    }

    @Test
    @DisplayName("分页查询 - 第一页")
    void testListDatasets_FirstPage() {
        Page<Dataset> page = new Page<>(1, 20);
        page.setRecords(Collections.singletonList(dataset));

        when(datasetMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(1);
        pageRequest.setPageSize(20);

        Page<Dataset> result = datasetService.listDatasets(null, null, null, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getCurrent());
    }

    @Test
    @DisplayName("删除数据集 - 存在")
    void testDeleteDataset_Exists() {
        when(datasetMapper.selectById(1L)).thenReturn(dataset);
        when(datasetMapper.deleteById(1L)).thenReturn(1);

        datasetService.deleteDataset(1L);

        verify(datasetMapper).selectById(1L);
        verify(datasetMapper).deleteById(1L);
    }

    @Test
    @DisplayName("删除数据集 - 不存在")
    void testDeleteDataset_NotExists() {
        when(datasetMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> {
            datasetService.deleteDataset(999L);
        });
    }

    @Test
    @DisplayName("更新数据集 - 成功")
    void testUpdateDataset_Success() {
        when(datasetMapper.updateById(any(Dataset.class))).thenReturn(1);

        datasetService.updateDataset(dataset);

        verify(datasetMapper).updateById(any(Dataset.class));
    }

    @Test
    @DisplayName("更新数据集 - 失败")
    void testUpdateDataset_Failure() {
        when(datasetMapper.updateById(any(Dataset.class))).thenReturn(0);

        datasetService.updateDataset(dataset);

        verify(datasetMapper).updateById(any(Dataset.class));
    }
}
