package com.ctg.dataFab.etl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.common.enums.DataStatus;
import com.ctg.dataFab.common.exception.BusinessException;
import com.ctg.dataFab.etl.entity.EtlRule;
import com.ctg.dataFab.etl.entity.EtlTask;
import com.ctg.dataFab.etl.mapper.EtlRuleMapper;
import com.ctg.dataFab.etl.mapper.EtlTaskMapper;
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

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 清洗任务服务单元测试
 *
 * @author Developer
 * @since 2026-07-05
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("清洗任务服务测试")
class EtlTaskServiceImplTest {

    @Mock
    private EtlTaskMapper etlTaskMapper;

    @Mock
    private EtlRuleMapper etlRuleMapper;

    @Mock
    private DataSampleMapper dataSampleMapper;

    @Mock
    private DatasetService datasetService;

    @InjectMocks
    private EtlTaskServiceImpl etlTaskService;

    private EtlTask etlTask;
    private DataSample dataSample;
    private EtlRule etlRule;

    @BeforeEach
    void setUp() {
        etlTask = new EtlTask();
        etlTask.setId(1L);
        etlTask.setTaskName("测试任务");
        etlTask.setDatasetId(1L);
        etlTask.setStatus(0); // 待执行
        etlTask.setProcessedCount(0);
        etlTask.setSuccessCount(0);
        etlTask.setFailedCount(0);

        dataSample = new DataSample();
        dataSample.setId(10L);
        dataSample.setDatasetId(1L);
        dataSample.setStatus(DataStatus.NEW.getCode());

        etlRule = new EtlRule();
        etlRule.setId(1L);
        etlRule.setName("测试规则");
        etlRule.setEnabled(1);
        etlRule.setPriority(10);
    }

    @Test
    @DisplayName("创建任务 - 初始化状态")
    void testCreateTask_InitialStatus() {
        when(etlTaskMapper.insert(any(EtlTask.class))).thenAnswer(invocation -> {
            EtlTask task = invocation.getArgument(0);
            task.setId(1L);
            return 1;
        });

        Long id = etlTaskService.createTask(1L, "测试任务");

        assertNotNull(id);
        assertEquals(0, etlTask.getStatus()); // 待执行
        assertEquals(0, etlTask.getProcessedCount());
        verify(etlTaskMapper).insert(any(EtlTask.class));
    }

    @Test
    @DisplayName("获取任务 - 存在")
    void testGetTaskById_Exists() {
        when(etlTaskMapper.selectById(1L)).thenReturn(etlTask);

        EtlTask result = etlTaskService.getTaskById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("测试任务", result.getTaskName());
    }

    @Test
    @DisplayName("获取任务 - 不存在")
    void testGetTaskById_NotExists() {
        when(etlTaskMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> {
            etlTaskService.getTaskById(999L);
        });
    }

    @Test
    @DisplayName("分页查询任务 - 无过滤条件")
    void testListTasks_NoFilter() {
        Page<EtlTask> page = new Page<>(1, 20);
        page.setRecords(Collections.singletonList(etlTask));

        when(etlTaskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(1);
        pageRequest.setPageSize(20);

        Page<EtlTask> result = etlTaskService.listTasks(null, null, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("分页查询任务 - 按数据集过滤")
    void testListTasks_FilterByDataset() {
        Page<EtlTask> page = new Page<>(1, 20);
        page.setRecords(Collections.singletonList(etlTask));

        when(etlTaskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(1);
        pageRequest.setPageSize(20);

        Page<EtlTask> result = etlTaskService.listTasks(1L, null, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("分页查询任务 - 按状态过滤")
    void testListTasks_FilterByStatus() {
        Page<EtlTask> page = new Page<>(1, 20);
        page.setRecords(Collections.singletonList(etlTask));

        when(etlTaskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(1);
        pageRequest.setPageSize(20);

        Page<EtlTask> result = etlTaskService.listTasks(null, 0, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("执行任务 - 成功处理样本")
    void testExecuteTask_Success() {
        when(etlTaskMapper.selectById(1L)).thenReturn(etlTask);
        when(etlTaskMapper.updateById(any(EtlTask.class))).thenReturn(1);
        when(dataSampleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(dataSample));
        when(etlRuleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(etlRule));
        when(dataSampleMapper.updateById(any(DataSample.class))).thenReturn(1);

        etlTaskService.executeTask(1L);

        verify(etlTaskMapper, atLeastOnce()).updateById(any(EtlTask.class));
        verify(dataSampleMapper).updateById(any(DataSample.class));
        verify(datasetService).updateDatasetStats(1L);
    }

    @Test
    @DisplayName("执行任务 - 无样本")
    void testExecuteTask_NoSamples() {
        when(etlTaskMapper.selectById(1L)).thenReturn(etlTask);
        when(etlTaskMapper.updateById(any(EtlTask.class))).thenReturn(1);
        when(dataSampleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());
        when(etlRuleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(etlRule));

        etlTaskService.executeTask(1L);

        verify(etlTaskMapper, atLeastOnce()).updateById(any(EtlTask.class));
        verify(dataSampleMapper, never()).updateById(any(DataSample.class));
    }

    @Test
    @DisplayName("执行任务 - 状态不正确")
    void testExecuteTask_WrongStatus() {
        etlTask.setStatus(1); // 执行中
        when(etlTaskMapper.selectById(1L)).thenReturn(etlTask);

        assertThrows(BusinessException.class, () -> {
            etlTaskService.executeTask(1L);
        });
    }

    @Test
    @DisplayName("取消任务 - 执行中")
    void testCancelTask_Running() {
        etlTask.setStatus(1); // 执行中
        when(etlTaskMapper.selectById(1L)).thenReturn(etlTask);
        when(etlTaskMapper.updateById(any(EtlTask.class))).thenReturn(1);

        etlTaskService.cancelTask(1L);

        assertEquals(3, etlTask.getStatus()); // 失败
        assertEquals("任务被取消", etlTask.getErrorMessage());
        verify(etlTaskMapper).updateById(any(EtlTask.class));
    }

    @Test
    @DisplayName("取消任务 - 非执行中状态")
    void testCancelTask_NotRunning() {
        etlTask.setStatus(0); // 待执行
        when(etlTaskMapper.selectById(1L)).thenReturn(etlTask);

        assertThrows(BusinessException.class, () -> {
            etlTaskService.cancelTask(1L);
        });
    }

    @Test
    @DisplayName("取消任务 - 任务不存在")
    void testCancelTask_NotExists() {
        when(etlTaskMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> {
            etlTaskService.cancelTask(999L);
        });
    }

    @Test
    @DisplayName("执行任务 - 多个样本部分失败")
    void testExecuteTask_PartialFailure() {
        DataSample sample1 = new DataSample();
        sample1.setId(10L);
        sample1.setDatasetId(1L);

        DataSample sample2 = new DataSample();
        sample2.setId(11L);
        sample2.setDatasetId(1L);

        when(etlTaskMapper.selectById(1L)).thenReturn(etlTask);
        when(etlTaskMapper.updateById(any(EtlTask.class))).thenReturn(1);
        when(dataSampleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(sample1, sample2));
        when(etlRuleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(etlRule));
        when(dataSampleMapper.updateById(any(DataSample.class)))
                .thenReturn(1)
                .thenThrow(new RuntimeException("处理失败"));

        etlTaskService.executeTask(1L);

        verify(etlTaskMapper, atLeastOnce()).updateById(any(EtlTask.class));
        verify(datasetService).updateDatasetStats(1L);
    }
}
