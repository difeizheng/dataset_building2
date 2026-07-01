package com.ctg.dataFab.ingest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.common.enums.DataStatus;
import com.ctg.dataFab.common.exception.BusinessException;
import com.ctg.dataFab.ingest.dto.DatasetCreateRequest;
import com.ctg.dataFab.ingest.entity.DataSample;
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

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 数据集服务单元测试
 *
 * @author Developer
 * @since 2026-07-01
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("数据集服务测试")
class DatasetServiceImplTest {

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
    }

    @Test
    @DisplayName("创建数据集")
    void testCreateDataset() {
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
    @DisplayName("获取数据集 - 存在")
    void testGetDatasetById_Exists() {
        when(datasetMapper.selectById(1L)).thenReturn(dataset);

        Dataset result = datasetService.getDatasetById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("测试数据集", result.getName());
    }

    @Test
    @DisplayName("获取数据集 - 不存在")
    void testGetDatasetById_NotExists() {
        when(datasetMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> {
            datasetService.getDatasetById(999L);
        });
    }

    @Test
    @DisplayName("分页查询数据集")
    void testListDatasets() {
        Page<Dataset> page = new Page<>(1, 20);
        page.add(dataset);

        when(datasetMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(1);
        pageRequest.setPageSize(20);

        Page<Dataset> result = datasetService.listDatasets(null, null, null, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("更新数据集")
    void testUpdateDataset() {
        when(datasetMapper.updateById(any(Dataset.class))).thenReturn(1);

        datasetService.updateDataset(dataset);

        verify(datasetMapper).updateById(any(Dataset.class));
    }

    @Test
    @DisplayName("删除数据集")
    void testDeleteDataset() {
        when(datasetMapper.selectById(1L)).thenReturn(dataset);
        when(datasetMapper.deleteById(1L)).thenReturn(1);

        datasetService.deleteDataset(1L);

        verify(datasetMapper).deleteById(1L);
    }

    @Test
    @DisplayName("更新数据集统计信息")
    void testUpdateDatasetStats() {
        when(datasetMapper.selectById(1L)).thenReturn(dataset);
        when(dataSampleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(10L);
        when(dataSampleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(createSample(1024L), createSample(2048L)));
        when(datasetMapper.updateById(any(Dataset.class))).thenReturn(1);

        datasetService.updateDatasetStats(1L);

        verify(datasetMapper).updateById(any(Dataset.class));
    }

    private DataSample createSample(Long fileSize) {
        DataSample sample = new DataSample();
        sample.setDatasetId(1L);
        sample.setFileSize(fileSize);
        return sample;
    }
}
