package com.ctg.dataFab.qa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.common.enums.DataStatus;
import com.ctg.dataFab.common.exception.BusinessException;
import com.ctg.dataFab.ingest.entity.Dataset;
import com.ctg.dataFab.ingest.mapper.DatasetMapper;
import com.ctg.dataFab.ingest.service.DatasetService;
import com.ctg.dataFab.qa.dto.EvaluateRequest;
import com.ctg.dataFab.qa.dto.EvaluateResponse;
import com.ctg.dataFab.qa.entity.QaTask;
import com.ctg.dataFab.qa.mapper.QaTaskMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 质量评估服务单元测试
 *
 * @author Developer
 * @since 2026-07-01
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("质量评估服务测试")
class QaServiceImplTest {

    @Mock
    private QaTaskMapper qaTaskMapper;

    @Mock
    private DatasetMapper datasetMapper;

    @Mock
    private DatasetService datasetService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private QaServiceImpl qaService;

    private EvaluateRequest evaluateRequest;
    private QaTask qaTask;

    @BeforeEach
    void setUp() {
        evaluateRequest = new EvaluateRequest();
        evaluateRequest.setDatasetId(1L);
        evaluateRequest.setModality(1);
        evaluateRequest.setBatch("batch1");

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("completeness_rate", 99.8);
        metrics.put("grammar_accuracy", 99.0);
        metrics.put("duplicate_rate", 0.5);
        metrics.put("toxicity_rate", 0.05);
        metrics.put("domain_cv", 0.2);
        evaluateRequest.setMetrics(metrics);

        qaTask = new QaTask();
        qaTask.setId(1L);
        qaTask.setDatasetId(1L);
        qaTask.setModality(1);
        qaTask.setStatus(1);
        qaTask.setReviewStage(0);
    }

    @Test
    @DisplayName("执行质量评估 - 通过")
    void testEvaluate_Pass() {
        when(qaTaskMapper.insert(any(QaTask.class))).thenAnswer(invocation -> {
            QaTask task = invocation.getArgument(0);
            task.setId(1L);
            return 1;
        });
        when(qaTaskMapper.updateById(any(QaTask.class))).thenReturn(1);
        when(datasetService.getDatasetById(1L)).thenReturn(new Dataset());
        when(datasetMapper.updateById(any(Dataset.class))).thenReturn(1);

        EvaluateResponse response = qaService.evaluate(evaluateRequest);

        assertNotNull(response);
        assertTrue(response.getPassed());
        verify(qaTaskMapper).insert(any(QaTask.class));
    }

    @Test
    @DisplayName("执行质量评估 - 不通过")
    void testEvaluate_Fail() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("completeness_rate", 90.0); // 低于99.5%
        evaluateRequest.setMetrics(metrics);

        when(qaTaskMapper.insert(any(QaTask.class))).thenAnswer(invocation -> {
            QaTask task = invocation.getArgument(0);
            task.setId(1L);
            return 1;
        });
        when(qaTaskMapper.updateById(any(QaTask.class))).thenReturn(1);

        EvaluateResponse response = qaService.evaluate(evaluateRequest);

        assertNotNull(response);
        assertFalse(response.getPassed());
    }

    @Test
    @DisplayName("获取评估任务 - 存在")
    void testGetTaskById_Exists() {
        when(qaTaskMapper.selectById(1L)).thenReturn(qaTask);

        QaTask result = qaService.getTaskById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("获取评估任务 - 不存在")
    void testGetTaskById_NotExists() {
        when(qaTaskMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> {
            qaService.getTaskById(999L);
        });
    }

    @Test
    @DisplayName("分页查询评估任务")
    void testListTasks() {
        Page<QaTask> page = new Page<>(1, 20);
        page.add(qaTask);

        when(qaTaskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(1);
        pageRequest.setPageSize(20);

        Page<QaTask> result = qaService.listTasks(null, null, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("人工复审")
    void testManualReview() {
        when(qaTaskMapper.selectById(1L)).thenReturn(qaTask);
        when(qaTaskMapper.updateById(any(QaTask.class))).thenReturn(1);

        qaService.manualReview(1L, true, "审核通过", "reviewer1");

        verify(qaTaskMapper).updateById(any(QaTask.class));
    }

    @Test
    @DisplayName("人工复审 - 状态不正确")
    void testManualReview_WrongStage() {
        qaTask.setReviewStage(1); // 不是自动预审阶段
        when(qaTaskMapper.selectById(1L)).thenReturn(qaTask);

        assertThrows(BusinessException.class, () -> {
            qaService.manualReview(1L, true, "审核通过", "reviewer1");
        });
    }

    @Test
    @DisplayName("专家终审")
    void testExpertReview() {
        qaTask.setReviewStage(1);
        when(qaTaskMapper.selectById(1L)).thenReturn(qaTask);
        when(qaTaskMapper.updateById(any(QaTask.class))).thenReturn(1);

        qaService.expertReview(1L, true, "终审通过", "expert1");

        verify(qaTaskMapper).updateById(any(QaTask.class));
    }

    @Test
    @DisplayName("专家终审 - 状态不正确")
    void testExpertReview_WrongStage() {
        qaTask.setReviewStage(0); // 不是人工复审阶段
        when(qaTaskMapper.selectById(1L)).thenReturn(qaTask);

        assertThrows(BusinessException.class, () -> {
            qaService.expertReview(1L, true, "终审通过", "expert1");
        });
    }

    @Test
    @DisplayName("发布数据集")
    void testPublishDataset() {
        qaTask.setReviewStage(2);
        qaTask.setPassed(1);
        when(qaTaskMapper.selectById(1L)).thenReturn(qaTask);
        when(qaTaskMapper.updateById(any(QaTask.class))).thenReturn(1);
        when(datasetService.getDatasetById(1L)).thenReturn(new Dataset());
        when(datasetMapper.updateById(any(Dataset.class))).thenReturn(1);

        qaService.publishDataset(1L);

        verify(qaTaskMapper).updateById(any(QaTask.class));
        verify(datasetMapper).updateById(any(Dataset.class));
    }

    @Test
    @DisplayName("发布数据集 - 未通过终审")
    void testPublishDataset_NotApproved() {
        qaTask.setReviewStage(1); // 未通过专家终审
        qaTask.setPassed(1);
        when(qaTaskMapper.selectById(1L)).thenReturn(qaTask);

        assertThrows(BusinessException.class, () -> {
            qaService.publishDataset(1L);
        });
    }
}
