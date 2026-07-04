package com.ctg.dataFab.label.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.common.enums.LabelTaskStatus;
import com.ctg.dataFab.common.exception.BusinessException;
import com.ctg.dataFab.ingest.entity.DataSample;
import com.ctg.dataFab.ingest.mapper.DataSampleMapper;
import com.ctg.dataFab.ingest.service.DatasetService;
import com.ctg.dataFab.label.dto.CreateLabelTaskRequest;
import com.ctg.dataFab.label.dto.SubmitLabelRequest;
import com.ctg.dataFab.label.entity.LabelRecord;
import com.ctg.dataFab.label.entity.LabelTask;
import com.ctg.dataFab.label.mapper.LabelArbitrationMapper;
import com.ctg.dataFab.label.mapper.LabelRecordMapper;
import com.ctg.dataFab.label.mapper.LabelTaskMapper;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 标注任务服务边界条件单元测试
 *
 * @author Developer
 * @since 2026-07-05
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("标注任务服务边界测试")
class LabelTaskServiceImplBoundaryTest {

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

    private LabelTask labelTask;
    private DataSample sample;

    @BeforeEach
    void setUp() {
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
    @DisplayName("创建任务 - 无样本")
    void testCreateTask_NoSamples() throws Exception {
        CreateLabelTaskRequest request = new CreateLabelTaskRequest();
        request.setTaskName("测试任务");
        request.setDatasetId(1L);
        request.setModality(1);
        request.setLabelType("classification");
        request.setLabelSchema("[\"cat\",\"dog\"]");
        request.setDoubleBlind(0); // 非双盲
        request.setAnnotatorIds(Arrays.asList(100L));
        request.setSopDescription("SOP");

        when(dataSampleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(dataSampleMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());
        when(labelTaskMapper.insert(any(LabelTask.class))).thenAnswer(invocation -> {
            LabelTask task = invocation.getArgument(0);
            task.setId(1L);
            return 1;
        });
        when(labelTaskMapper.updateById(any(LabelTask.class))).thenReturn(1);
        when(objectMapper.writeValueAsString(any())).thenReturn("[100]");

        Long taskId = labelTaskService.createTask(request);

        assertNotNull(taskId);
        verify(labelTaskMapper).insert(any(LabelTask.class));
    }

    @Test
    @DisplayName("创建任务 - 单标注人非双盲")
    void testCreateTask_SingleAnnotatorNonDoubleBlind() throws Exception {
        CreateLabelTaskRequest request = new CreateLabelTaskRequest();
        request.setTaskName("测试任务");
        request.setDatasetId(1L);
        request.setModality(1);
        request.setLabelType("classification");
        request.setLabelSchema("[\"cat\"]");
        request.setDoubleBlind(0); // 非双盲
        request.setAnnotatorIds(Arrays.asList(100L));
        request.setSopDescription("SOP");

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
        when(objectMapper.writeValueAsString(any())).thenReturn("[100]");

        Long taskId = labelTaskService.createTask(request);

        assertNotNull(taskId);
        verify(labelTaskMapper).insert(any(LabelTask.class));
    }

    @Test
    @DisplayName("提交标注 - 零耗时")
    void testSubmitLabel_ZeroDuration() throws Exception {
        SubmitLabelRequest request = new SubmitLabelRequest();
        request.setTaskId(1L);
        request.setSampleId(10L);
        request.setAnnotatorId(100L);
        request.setAnnotations("[\"cat\"]");
        request.setDurationSeconds(0L);

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
    @DisplayName("提交标注 - 长时间标注")
    void testSubmitLabel_LongDuration() throws Exception {
        SubmitLabelRequest request = new SubmitLabelRequest();
        request.setTaskId(1L);
        request.setSampleId(10L);
        request.setAnnotatorId(100L);
        request.setAnnotations("[\"cat\"]");
        request.setDurationSeconds(3600L); // 1小时

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
    @DisplayName("计算IAA分数 - 有记录")
    void testCalculateIaaScore_WithRecords() {
        LabelRecord record1 = new LabelRecord();
        record1.setId(1L);
        record1.setTaskId(1L);
        record1.setSampleId(10L);
        record1.setAnnotations("[\"cat\"]");

        LabelRecord record2 = new LabelRecord();
        record2.setId(2L);
        record2.setTaskId(1L);
        record2.setSampleId(10L);
        record2.setAnnotations("[\"cat\"]");

        when(labelTaskMapper.selectById(1L)).thenReturn(labelTask);
        when(labelRecordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(record1, record2));

        Double score = labelTaskService.calculateIaaScore(1L);

        assertNotNull(score);
    }

    @Test
    @DisplayName("触发重标 - 无记录")
    void testTriggerRelabel_NoRecords() throws Exception {
        when(labelTaskMapper.selectById(1L)).thenReturn(labelTask);
        when(labelRecordMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(0);
        when(labelTaskMapper.updateById(any(LabelTask.class))).thenReturn(1);

        labelTaskService.triggerRelabel(1L);

        verify(labelRecordMapper).delete(any(LambdaQueryWrapper.class));
        verify(labelTaskMapper).updateById(any(LabelTask.class));
    }

    @Test
    @DisplayName("分页查询 - 空结果")
    void testListTasks_EmptyResult() {
        Page<LabelTask> page = new Page<>(1, 20);
        page.setRecords(Collections.emptyList());

        when(labelTaskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(1);
        pageRequest.setPageSize(20);

        Page<LabelTask> result = labelTaskService.listTasks(null, null, pageRequest);

        assertNotNull(result);
        assertEquals(0, result.getRecords().size());
    }

    @Test
    @DisplayName("获取任务 - 不存在")
    void testGetTaskById_NotExists() {
        when(labelTaskMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> {
            labelTaskService.getTaskById(999L);
        });
    }
}
