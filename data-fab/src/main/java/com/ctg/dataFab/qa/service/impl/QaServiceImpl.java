package com.ctg.dataFab.qa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.common.enums.DataModality;
import com.ctg.dataFab.common.enums.DataStatus;
import com.ctg.dataFab.common.exception.BusinessException;
import com.ctg.dataFab.ingest.entity.Dataset;
import com.ctg.dataFab.ingest.mapper.DatasetMapper;
import com.ctg.dataFab.ingest.service.DatasetService;
import com.ctg.dataFab.qa.dto.EvaluateRequest;
import com.ctg.dataFab.qa.dto.EvaluateResponse;
import com.ctg.dataFab.qa.engine.QualityThresholdEngine;
import com.ctg.dataFab.qa.entity.QaTask;
import com.ctg.dataFab.qa.mapper.QaTaskMapper;
import com.ctg.dataFab.qa.service.QaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 质量评估服务实现
 * 实现 M2 四模态质量阈值检测 + 三审三校流程
 *
 * @author Developer
 * @since 2026-07-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QaServiceImpl implements QaService {

    private final QaTaskMapper qaTaskMapper;
    private final DatasetMapper datasetMapper;
    private final DatasetService datasetService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EvaluateResponse evaluate(EvaluateRequest request) {
        log.info("开始质量评估: 数据集ID={}, 模态={}", request.getDatasetId(), request.getModality());

        // 创建评估任务
        QaTask task = new QaTask();
        task.setDatasetId(request.getDatasetId());
        task.setModality(request.getModality());
        task.setBatch(request.getBatch());
        task.setStatus(1); // 执行中
        task.setReviewStage(0); // 自动预审

        qaTaskMapper.insert(task);

        // 执行质量评估
        DataModality modality = DataModality.fromCode(request.getModality());
        QualityThresholdEngine.QualityResult result =
                QualityThresholdEngine.evaluate(modality, request.getMetrics());

        // 更新任务结果
        try {
            task.setMetrics(objectMapper.writeValueAsString(result.getMetrics()));
            task.setGates(objectMapper.writeValueAsString(result.getGates()));
        } catch (Exception e) {
            log.error("序列化评估结果失败", e);
        }

        task.setPassed(result.isPassed() ? 1 : 0);
        task.setStatus(result.isPassed() ? 2 : 3); // 通过/未通过
        qaTaskMapper.updateById(task);

        // 如果通过，更新数据集状态
        if (result.isPassed()) {
            Dataset dataset = datasetService.getDatasetById(request.getDatasetId());
            dataset.setStatus(DataStatus.QA_PASS.getCode());
            datasetMapper.updateById(dataset);
        }

        // 构建响应
        EvaluateResponse response = new EvaluateResponse();
        response.setTaskId(task.getId());
        response.setPassed(result.isPassed());
        response.setMetrics(result.getMetrics());
        response.setGates(result.getGates().stream().map(gate -> {
            EvaluateResponse.GateInfo info = new EvaluateResponse.GateInfo();
            info.setName(gate.getName());
            info.setThreshold(gate.getThreshold());
            info.setActual(gate.getActual());
            info.setPassed(gate.isPassed());
            info.setOperator(gate.getOperator());
            return info;
        }).collect(Collectors.toList()));

        log.info("质量评估完成: 任务ID={}, 通过={}", task.getId(), result.isPassed());
        return response;
    }

    @Override
    public QaTask getTaskById(Long id) {
        QaTask task = qaTaskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("评估任务不存在");
        }
        return task;
    }

    @Override
    public Page<QaTask> listTasks(Long datasetId, Integer status, PageRequest pageRequest) {
        LambdaQueryWrapper<QaTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(datasetId != null, QaTask::getDatasetId, datasetId)
               .eq(status != null, QaTask::getStatus, status)
               .orderByDesc(QaTask::getCreateTime);

        Page<QaTask> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        return qaTaskMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void manualReview(Long taskId, boolean passed, String comment, String reviewer) {
        QaTask task = getTaskById(taskId);
        if (task.getReviewStage() != 0) {
            throw new BusinessException("任务不在自动预审阶段");
        }

        task.setReviewStage(1); // 人工复审
        task.setPassed(passed ? 1 : 0);
        task.setReviewComment(comment);
        task.setReviewer(reviewer);
        task.setReviewTime(LocalDateTime.now());
        task.setStatus(passed ? 2 : 3);

        qaTaskMapper.updateById(task);
        log.info("人工复审完成: 任务ID={}, 通过={}", taskId, passed);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void expertReview(Long taskId, boolean passed, String comment, String reviewer) {
        QaTask task = getTaskById(taskId);
        if (task.getReviewStage() != 1) {
            throw new BusinessException("任务不在人工复审阶段");
        }

        task.setReviewStage(2); // 专家终审
        task.setPassed(passed ? 1 : 0);
        task.setReviewComment(comment);
        task.setReviewer(reviewer);
        task.setReviewTime(LocalDateTime.now());
        task.setStatus(passed ? 2 : 3);

        qaTaskMapper.updateById(task);
        log.info("专家终审完成: 任务ID={}, 通过={}", taskId, passed);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishDataset(Long taskId) {
        QaTask task = getTaskById(taskId);
        if (task.getReviewStage() != 2 || task.getPassed() != 1) {
            throw new BusinessException("任务未通过专家终审，无法发布");
        }

        task.setReviewStage(3); // 发布
        qaTaskMapper.updateById(task);

        // 更新数据集状态为已发布
        Dataset dataset = datasetService.getDatasetById(task.getDatasetId());
        dataset.setStatus(DataStatus.PUBLISHED.getCode());
        datasetMapper.updateById(dataset);

        log.info("数据集发布成功: 任务ID={}, 数据集ID={}", taskId, task.getDatasetId());
    }
}
