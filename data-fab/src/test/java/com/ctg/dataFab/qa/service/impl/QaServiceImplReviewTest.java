package com.ctg.dataFab.qa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.common.exception.BusinessException;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 质量评估服务复审流程单元测试
 *
 * @author Developer
 * @since 2026-07-05
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("质量评估服务复审测试")
class QaServiceImplReviewTest {

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

    private QaTask qaTask;

    @BeforeEach
    void setUp() {
        qaTask = new QaTask();
        qaTask.setId(1L);
        qaTask.setDatasetId(1L);
        qaTask.setModality(1);
        qaTask.setStatus(1);
        qaTask.setReviewStage(0); // 自动预审阶段
        qaTask.setPassed(0);
    }

    @Test
    @DisplayName("人工复审 - 通过")
    void testManualReview_Pass() {
        when(qaTaskMapper.selectById(1L)).thenReturn(qaTask);
        when(qaTaskMapper.updateById(any(QaTask.class))).thenReturn(1);

        qaService.manualReview(1L, true, "审核通过", "reviewer1");

        verify(qaTaskMapper).selectById(1L);
        verify(qaTaskMapper).updateById(any(QaTask.class));
    }

    @Test
    @DisplayName("人工复审 - 不通过")
    void testManualReview_Reject() {
        when(qaTaskMapper.selectById(1L)).thenReturn(qaTask);
        when(qaTaskMapper.updateById(any(QaTask.class))).thenReturn(1);

        qaService.manualReview(1L, false, "审核不通过", "reviewer1");

        verify(qaTaskMapper).selectById(1L);
        verify(qaTaskMapper).updateById(any(QaTask.class));
    }

    @Test
    @DisplayName("人工复审 - 错误阶段")
    void testManualReview_WrongStage() {
        qaTask.setReviewStage(1); // 不是自动预审阶段
        when(qaTaskMapper.selectById(1L)).thenReturn(qaTask);

        assertThrows(BusinessException.class, () -> {
            qaService.manualReview(1L, true, "审核通过", "reviewer1");
        });
    }

    @Test
    @DisplayName("专家终审 - 通过")
    void testExpertReview_Pass() {
        qaTask.setReviewStage(1); // 人工复审阶段
        when(qaTaskMapper.selectById(1L)).thenReturn(qaTask);
        when(qaTaskMapper.updateById(any(QaTask.class))).thenReturn(1);

        qaService.expertReview(1L, true, "终审通过", "expert1");

        verify(qaTaskMapper).selectById(1L);
        verify(qaTaskMapper).updateById(any(QaTask.class));
    }

    @Test
    @DisplayName("专家终审 - 不通过")
    void testExpertReview_Reject() {
        qaTask.setReviewStage(1);
        when(qaTaskMapper.selectById(1L)).thenReturn(qaTask);
        when(qaTaskMapper.updateById(any(QaTask.class))).thenReturn(1);

        qaService.expertReview(1L, false, "终审不通过", "expert1");

        verify(qaTaskMapper).selectById(1L);
        verify(qaTaskMapper).updateById(any(QaTask.class));
    }

    @Test
    @DisplayName("专家终审 - 错误阶段")
    void testExpertReview_WrongStage() {
        qaTask.setReviewStage(0); // 不是人工复审阶段
        when(qaTaskMapper.selectById(1L)).thenReturn(qaTask);

        assertThrows(BusinessException.class, () -> {
            qaService.expertReview(1L, true, "终审通过", "expert1");
        });
    }

    @Test
    @DisplayName("发布数据集 - 成功")
    void testPublishDataset_Success() {
        qaTask.setReviewStage(2); // 已通过终审
        qaTask.setPassed(1);
        when(qaTaskMapper.selectById(1L)).thenReturn(qaTask);
        when(qaTaskMapper.updateById(any(QaTask.class))).thenReturn(1);
        when(datasetService.getDatasetById(1L)).thenReturn(new com.ctg.dataFab.ingest.entity.Dataset());
        when(datasetMapper.updateById(any())).thenReturn(1);

        qaService.publishDataset(1L);

        verify(qaTaskMapper).updateById(any(QaTask.class));
        verify(datasetMapper).updateById(any());
    }

    @Test
    @DisplayName("发布数据集 - 未通过终审")
    void testPublishDataset_NotApproved() {
        qaTask.setReviewStage(1); // 未通过终审
        qaTask.setPassed(1);
        when(qaTaskMapper.selectById(1L)).thenReturn(qaTask);

        assertThrows(BusinessException.class, () -> {
            qaService.publishDataset(1L);
        });
    }

    @Test
    @DisplayName("发布数据集 - 评估未通过")
    void testPublishDataset_NotPassed() {
        qaTask.setReviewStage(2);
        qaTask.setPassed(0); // 评估未通过
        when(qaTaskMapper.selectById(1L)).thenReturn(qaTask);

        assertThrows(BusinessException.class, () -> {
            qaService.publishDataset(1L);
        });
    }

    @Test
    @DisplayName("分页查询 - 按数据集过滤")
    void testListTasks_FilterByDataset() {
        Page<QaTask> page = new Page<>(1, 20);
        page.setRecords(Collections.singletonList(qaTask));

        when(qaTaskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(1);
        pageRequest.setPageSize(20);

        Page<QaTask> result = qaService.listTasks(1L, null, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("分页查询 - 按状态过滤")
    void testListTasks_FilterByStatus() {
        Page<QaTask> page = new Page<>(1, 20);
        page.setRecords(Collections.singletonList(qaTask));

        when(qaTaskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(1);
        pageRequest.setPageSize(20);

        Page<QaTask> result = qaService.listTasks(null, 1, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("获取任务 - 不存在")
    void testGetTaskById_NotExists() {
        when(qaTaskMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> {
            qaService.getTaskById(999L);
        });
    }
}
