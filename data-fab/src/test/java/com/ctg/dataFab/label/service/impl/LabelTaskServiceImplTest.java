package com.ctg.dataFab.label.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.common.enums.DataStatus;
import com.ctg.dataFab.common.enums.LabelTaskStatus;
import com.ctg.dataFab.common.exception.BusinessException;
import com.ctg.dataFab.ingest.entity.DataSample;
import com.ctg.dataFab.ingest.mapper.DataSampleMapper;
import com.ctg.dataFab.ingest.service.DatasetService;
import com.ctg.dataFab.label.dto.CreateLabelTaskRequest;
import com.ctg.dataFab.label.dto.SubmitLabelRequest;
import com.ctg.dataFab.label.engine.IaaEngine;
import com.ctg.dataFab.label.entity.LabelArbitration;
import com.ctg.dataFab.label.entity.LabelRecord;
import com.ctg.dataFab.label.entity.LabelTask;
import com.ctg.dataFab.label.mapper.LabelArbitrationMapper;
import com.ctg.dataFab.label.mapper.LabelRecordMapper;
import com.ctg.dataFab.label.mapper.LabelTaskMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 标注任务服务单元测试
 *
 * @author Developer
 * @since 2026-07-01
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("标注任务服务测试")
class LabelTaskServiceImplTest {

    @Mock
    private LabelTaskMapper labelTaskMapper;

    @Mock
    private LabelRecordMapper labelRecordMapper;

    @Mock
    private LabelArbitrationMapper labelArbitrationMapper;

    @Mock
    private DataSampleMapper dataSampleMapper;

    @Mock
    private DatasetService datasetService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private LabelTaskServiceImpl labelTaskService;

    private CreateLabelTaskRequest createRequest;
    private LabelTask labelTask;
    private DataSample sample;

    @BeforeEach
    void setUp() {
        createRequest = new CreateLabelTaskRequest();
        createRequest.setTaskName("测试标注任务");
        createRequest.setDatasetId(1L);
        createRequest.setModality(1);
        createRequest.setLabelType("classification");
        createRequest.setLabelSchema("[\"cat\",\"dog\"]");
        createRequest.setDoubleBlind(1);
        createRequest.setAnnotatorIds(Arrays.asList(100L, 200L));
        createRequest.setArbitratorId(300L);
        createRequest.setSopDescription("SOP描述");

        labelTask = new LabelTask();
        labelTask.setId(1L);
        labelTask.setTaskName("测试标注任务");
        labelTask.setDatasetId(1L);
        labelTask.setAnnotatorCount(2);
        labelTask.setDoubleBlind(1);
        labelTask.setStatus(LabelTaskStatus.IN_PROGRESS.getCode());
        labelTask.setArbitratorId(300L);
        labelTask.setTotalSamples(5);

        sample = new DataSample();
        sample.setId(10L);
        sample.setDatasetId(1L);
    }

    @Test
    @DisplayName("创建标注任务 - 成功")
    void testCreateTask_Success() throws Exception {
        when(dataSampleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);
        when(dataSampleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(sample));
        when(labelTaskMapper.insert(any(LabelTask.class))).thenAnswer(invocation -> {
            LabelTask task = invocation.getArgument(0);
            task.setId(1L);
            return 1;
        });
        when(labelTaskMapper.updateById(any(LabelTask.class))).thenReturn(1);
        when(labelRecordMapper.insert(any(LabelRecord.class))).thenReturn(1);
        when(objectMapper.writeValueAsString(any())).thenReturn("[100,200]");

        Long taskId = labelTaskService.createTask(createRequest);

        assertNotNull(taskId);
        verify(labelTaskMapper).insert(any(LabelTask.class));
    }

    @Test
    @DisplayName("创建标注任务 - 双盲标注少于2人失败")
    void testCreateTask_DoubleBlind_TooFewAnnotators() {
        createRequest.setAnnotatorIds(Collections.singletonList(100L));

        assertThrows(BusinessException.class, () -> {
            labelTaskService.createTask(createRequest);
        });
    }

    @Test
    @DisplayName("获取任务 - 存在")
    void testGetTaskById_Exists() {
        when(labelTaskMapper.selectById(1L)).thenReturn(labelTask);

        LabelTask result = labelTaskService.getTaskById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("获取任务 - 不存在")
    void testGetTaskById_NotExists() {
        when(labelTaskMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> {
            labelTaskService.getTaskById(999L);
        });
    }

    @Test
    @DisplayName("分页查询任务")
    void testListTasks() {
        Page<LabelTask> page = new Page<>(1, 20);
        page.setRecords(Collections.singletonList(labelTask));

        when(labelTaskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(1);
        pageRequest.setPageSize(20);

        Page<LabelTask> result = labelTaskService.listTasks(null, null, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("提交标注 - 成功")
    void testSubmitLabel_Success() throws Exception {
        SubmitLabelRequest request = new SubmitLabelRequest();
        request.setTaskId(1L);
        request.setSampleId(10L);
        request.setAnnotatorId(100L);
        request.setAnnotations("[\"cat\"]");
        request.setDurationSeconds(30L);

        LabelRecord record = new LabelRecord();
        record.setId(1L);
        record.setTaskId(1L);
        record.setSampleId(10L);
        record.setAnnotatorId(100L);

        when(labelRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(record);
        when(labelRecordMapper.updateById(any(LabelRecord.class))).thenReturn(1);
        when(labelRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        when(labelTaskMapper.selectById(1L)).thenReturn(labelTask);
        when(labelTaskMapper.updateById(any(LabelTask.class))).thenReturn(1);
        when(labelRecordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(record));

        labelTaskService.submitLabel(request);

        verify(labelRecordMapper).updateById(any(LabelRecord.class));
    }

    @Test
    @DisplayName("提交标注 - 记录不存在")
    void testSubmitLabel_RecordNotFound() {
        SubmitLabelRequest request = new SubmitLabelRequest();
        request.setTaskId(1L);
        request.setSampleId(10L);
        request.setAnnotatorId(100L);

        when(labelRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        assertThrows(BusinessException.class, () -> {
            labelTaskService.submitLabel(request);
        });
    }

    @Test
    @DisplayName("触发仲裁 - 成功")
    void testTriggerArbitration_Success() {
        labelTask.setStatus(LabelTaskStatus.ARBITRATING.getCode());
        when(labelTaskMapper.selectById(1L)).thenReturn(labelTask);

        labelTaskService.triggerArbitration(1L);

        // Should not throw
    }

    @Test
    @DisplayName("触发仲裁 - 状态不正确")
    void testTriggerArbitration_WrongStatus() {
        labelTask.setStatus(LabelTaskStatus.IN_PROGRESS.getCode());
        when(labelTaskMapper.selectById(1L)).thenReturn(labelTask);

        assertThrows(BusinessException.class, () -> {
            labelTaskService.triggerArbitration(1L);
        });
    }

    @Test
    @DisplayName("计算IAA分数 - 无记录返回0")
    void testCalculateIaaScore_NoRecords() {
        when(labelTaskMapper.selectById(1L)).thenReturn(labelTask);
        when(labelRecordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        Double score = labelTaskService.calculateIaaScore(1L);

        assertEquals(0.0, score);
    }

    @Test
    @DisplayName("触发整体重标")
    void testTriggerRelabel() throws Exception {
        when(labelTaskMapper.selectById(1L)).thenReturn(labelTask);
        when(labelRecordMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(2);
        when(labelRecordMapper.insert(any(LabelRecord.class))).thenReturn(1);
        when(labelTaskMapper.updateById(any(LabelTask.class))).thenReturn(1);
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(Arrays.asList(100L, 200L));
        when(dataSampleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(sample));

        labelTaskService.triggerRelabel(1L);

        verify(labelRecordMapper).delete(any(LambdaQueryWrapper.class));
        verify(labelTaskMapper).updateById(any(LabelTask.class));
    }
}
