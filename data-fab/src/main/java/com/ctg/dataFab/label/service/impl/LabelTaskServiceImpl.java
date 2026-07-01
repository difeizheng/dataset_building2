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
import com.ctg.dataFab.label.service.LabelTaskService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 标注任务服务实现
 *
 * @author Developer
 * @since 2026-07-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LabelTaskServiceImpl implements LabelTaskService {

    private final LabelTaskMapper labelTaskMapper;
    private final LabelRecordMapper labelRecordMapper;
    private final LabelArbitrationMapper labelArbitrationMapper;
    private final DataSampleMapper dataSampleMapper;
    private final DatasetService datasetService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTask(CreateLabelTaskRequest request) {
        log.info("创建标注任务: {}", request.getTaskName());

        // 双盲标注校验：至少需要2名标注员
        if (request.getDoubleBlind() != null && request.getDoubleBlind() == 1) {
            if (request.getAnnotatorIds() == null || request.getAnnotatorIds().size() < 2) {
                throw new BusinessException("双盲标注至少需要2名标注员");
            }
        }

        LabelTask task = new LabelTask();
        task.setDatasetId(request.getDatasetId());
        task.setTaskName(request.getTaskName());
        task.setModality(request.getModality());
        task.setLabelType(request.getLabelType());
        task.setLabelSchema(request.getLabelSchema());
        task.setDoubleBlind(request.getDoubleBlind() != null ? request.getDoubleBlind() : 1);
        task.setAnnotatorCount(request.getAnnotatorIds() != null ? request.getAnnotatorIds().size() : 0);
        task.setArbitratorId(request.getArbitratorId());
        task.setSopDescription(request.getSopDescription());
        task.setStatus(LabelTaskStatus.PENDING.getCode());

        // 统计数据集样本数量
        LambdaQueryWrapper<DataSample> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DataSample::getDatasetId, request.getDatasetId());
        Long sampleCount = dataSampleMapper.selectCount(wrapper);
        task.setTotalSamples(sampleCount.intValue());
        task.setLabeledCount(0);

        try {
            task.setAnnotatorIds(objectMapper.writeValueAsString(request.getAnnotatorIds()));
        } catch (Exception e) {
            log.error("序列化标注员ID列表失败", e);
        }

        labelTaskMapper.insert(task);

        // 为每个样本创建标注记录
        List<DataSample> samples = dataSampleMapper.selectList(wrapper);
        for (DataSample sample : samples) {
            if (request.getAnnotatorIds() != null) {
                for (Long annotatorId : request.getAnnotatorIds()) {
                    LabelRecord record = new LabelRecord();
                    record.setTaskId(task.getId());
                    record.setSampleId(sample.getId());
                    record.setAnnotatorId(annotatorId);
                    record.setStatus(0); // 待标注
                    labelRecordMapper.insert(record);
                }
            }
        }

        // 更新任务状态为标注中
        task.setStatus(LabelTaskStatus.IN_PROGRESS.getCode());
        labelTaskMapper.updateById(task);

        log.info("标注任务创建成功, ID: {}, 样本数: {}", task.getId(), task.getTotalSamples());
        return task.getId();
    }

    @Override
    public LabelTask getTaskById(Long id) {
        LabelTask task = labelTaskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("标注任务不存在");
        }
        return task;
    }

    @Override
    public Page<LabelTask> listTasks(Long datasetId, Integer status, PageRequest pageRequest) {
        LambdaQueryWrapper<LabelTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(datasetId != null, LabelTask::getDatasetId, datasetId)
               .eq(status != null, LabelTask::getStatus, status)
               .orderByDesc(LabelTask::getCreateTime);

        Page<LabelTask> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        return labelTaskMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitLabel(SubmitLabelRequest request) {
        log.info("提交标注: 任务ID={}, 样本ID={}, 标注员ID={}",
                request.getTaskId(), request.getSampleId(), request.getAnnotatorId());

        // 查找标注记录
        LambdaQueryWrapper<LabelRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelRecord::getTaskId, request.getTaskId())
               .eq(LabelRecord::getSampleId, request.getSampleId())
               .eq(LabelRecord::getAnnotatorId, request.getAnnotatorId());

        LabelRecord record = labelRecordMapper.selectOne(wrapper);
        if (record == null) {
            throw new BusinessException("标注记录不存在");
        }

        record.setAnnotations(request.getAnnotations());
        record.setStatus(1); // 已标注
        record.setDurationSeconds(request.getDurationSeconds());
        record.setSubmitTime(LocalDateTime.now());
        labelRecordMapper.updateById(record);

        // 更新任务已标注数量
        LabelTask task = getTaskById(request.getTaskId());
        LambdaQueryWrapper<LabelRecord> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(LabelRecord::getTaskId, task.getId())
                   .eq(LabelRecord::getStatus, 1);
        Long labeledCount = labelRecordMapper.selectCount(countWrapper);
        task.setLabeledCount(labeledCount.intValue());

        // 检查是否所有标注员都已完成该样本的标注
        if (task.getDoubleBlind() == 1) {
            checkAndProcessIaa(task, request.getSampleId());
        }

        labelTaskMapper.updateById(task);
        log.info("标注提交成功, 任务ID: {}, 已标注: {}/{}", task.getId(), labeledCount, task.getTotalSamples() * task.getAnnotatorCount());
    }

    /**
     * 检查并处理IAA
     * H3修复: 等全部标注员提交后再算; 多人场景接入Fleiss Kappa; 加并发保护
     */
    private void checkAndProcessIaa(LabelTask task, Long sampleId) {
        // 获取该样本的所有标注记录
        LambdaQueryWrapper<LabelRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelRecord::getTaskId, task.getId())
               .eq(LabelRecord::getSampleId, sampleId)
               .eq(LabelRecord::getStatus, 1); // 已标注
        List<LabelRecord> records = labelRecordMapper.selectList(wrapper);

        // H3修复: 必须等全部标注员都提交后才计算IAA
        int expectedAnnotators = task.getAnnotatorCount() != null ? task.getAnnotatorCount() : 2;
        if (records.size() < expectedAnnotators) {
            log.debug("等待更多标注员提交: 样本ID={}, 已提交={}/{}", sampleId, records.size(), expectedAnnotators);
            return; // 还未完成全部标注
        }

        if (records.size() < 2) {
            return; // 至少需要2名标注员
        }

        // H3修复: 并发保护 - 检查是否已经处理过该样本的IAA
        LambdaQueryWrapper<LabelArbitration> arbWrapper = new LambdaQueryWrapper<>();
        arbWrapper.eq(LabelArbitration::getTaskId, task.getId())
                  .eq(LabelArbitration::getSampleId, sampleId);
        Long existingArbCount = labelArbitrationMapper.selectCount(arbWrapper);
        if (existingArbCount > 0) {
            log.debug("该样本已处理过IAA, 跳过: 样本ID={}", sampleId);
            return;
        }

        // H3修复: 根据标注员数量选择Cohen's Kappa或Fleiss' Kappa
        double kappa;
        if (records.size() == 2) {
            // 2名标注员: 使用Cohen's Kappa
            String[] labels1 = parseAnnotations(records.get(0).getAnnotations());
            String[] labels2 = parseAnnotations(records.get(1).getAnnotations());

            if (labels1.length == 0 || labels2.length == 0) {
                return;
            }

            kappa = IaaEngine.calculateCohenKappa(labels1, labels2);
        } else {
            // 3名及以上标注员: 使用Fleiss' Kappa
            kappa = calculateFleissKappaForSample(records);
        }

        IaaEngine.IaaDecision decision = IaaEngine.decide(kappa);

        log.info("IAA计算结果: 样本ID={}, 标注员数={}, Kappa={}, 决策={}", sampleId, records.size(), kappa, decision);

        switch (decision) {
            case ACCEPT:
                // 采纳标注，更新样本状态为已标注
                DataSample sample = dataSampleMapper.selectById(sampleId);
                if (sample != null) {
                    sample.setStatus(DataStatus.LABELED.getCode());
                    dataSampleMapper.updateById(sample);
                }
                break;
            case ARBITRATE:
                // 进入仲裁
                LabelArbitration arbitration = new LabelArbitration();
                arbitration.setTaskId(task.getId());
                arbitration.setSampleId(sampleId);
                arbitration.setOriginalKappa(kappa);
                arbitration.setArbitratorId(task.getArbitratorId());
                arbitration.setStatus(0); // 待仲裁
                try {
                    arbitration.setConflictAnnotations(
                            objectMapper.writeValueAsString(
                                    records.stream().map(LabelRecord::getAnnotations).collect(Collectors.toList())));
                } catch (Exception e) {
                    log.error("序列化冲突标注失败", e);
                }
                labelArbitrationMapper.insert(arbitration);
                task.setStatus(LabelTaskStatus.ARBITRATING.getCode());
                break;
            case RELABEL:
                // 触发重标
                triggerRelabelForSample(task, sampleId);
                break;
        }
    }

    /**
     * H3修复: 计算单个样本的Fleiss' Kappa (多名标注员)
     */
    private double calculateFleissKappaForSample(List<LabelRecord> records) {
        if (records.size() < 2) {
            return 0.0;
        }

        // 解析所有标注员的标签
        List<String[]> allLabels = records.stream()
                .map(r -> parseAnnotations(r.getAnnotations()))
                .filter(labels -> labels.length > 0)
                .collect(Collectors.toList());

        if (allLabels.size() < 2) {
            return 0.0;
        }

        // 找出最大标签数量 (所有标注员应该标注相同数量的项目)
        int maxLabels = allLabels.stream().mapToInt(arr -> arr.length).max().orElse(0);
        if (maxLabels == 0) {
            return 0.0;
        }

        // 收集所有唯一的类别
        Set<String> allCategories = new HashSet<>();
        for (String[] labels : allLabels) {
            for (String label : labels) {
                allCategories.add(label);
            }
        }

        int categories = allCategories.size();
        if (categories == 0) {
            return 0.0;
        }

        // 构建Fleiss' Kappa矩阵: [样本数][类别数]
        // 这里每个"样本"实际上是一个标注位置 (如图像中的第i个对象)
        int[][] annotations = new int[maxLabels][categories];
        List<String> categoryList = new ArrayList<>(allCategories);

        for (String[] labels : allLabels) {
            for (int j = 0; j < labels.length; j++) {
                int categoryIndex = categoryList.indexOf(labels[j]);
                if (categoryIndex >= 0) {
                    annotations[j][categoryIndex]++;
                }
            }
        }

        return IaaEngine.calculateFleissKappa(annotations, categories);
    }

    /**
     * 解析标注结果为标签数组
     */
    private String[] parseAnnotations(String annotationsJson) {
        try {
            List<String> labels = objectMapper.readValue(annotationsJson, new TypeReference<List<String>>() {});
            return labels.toArray(new String[0]);
        } catch (Exception e) {
            log.warn("解析标注结果失败: {}", annotationsJson, e);
            return new String[0];
        }
    }

    /**
     * 触发单个样本重标
     */
    private void triggerRelabelForSample(LabelTask task, Long sampleId) {
        log.warn("触发重标: 任务ID={}, 样本ID={}, Kappa<0.70", task.getId(), sampleId);

        // 删除原有标注记录
        LambdaQueryWrapper<LabelRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelRecord::getTaskId, task.getId())
               .eq(LabelRecord::getSampleId, sampleId);
        labelRecordMapper.delete(wrapper);

        // 重新创建标注记录
        try {
            List<Long> annotatorIds = objectMapper.readValue(task.getAnnotatorIds(), new TypeReference<List<Long>>() {});
            for (Long annotatorId : annotatorIds) {
                LabelRecord record = new LabelRecord();
                record.setTaskId(task.getId());
                record.setSampleId(sampleId);
                record.setAnnotatorId(annotatorId);
                record.setStatus(0); // 待标注
                labelRecordMapper.insert(record);
            }
        } catch (Exception e) {
            log.error("重新创建标注记录失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void triggerArbitration(Long taskId) {
        LabelTask task = getTaskById(taskId);
        if (task.getStatus() != LabelTaskStatus.ARBITRATING.getCode()) {
            throw new BusinessException("任务状态不是仲裁中");
        }
        log.info("触发仲裁, 任务ID: {}", taskId);
    }

    @Override
    public Double calculateIaaScore(Long taskId) {
        LabelTask task = getTaskById(taskId);

        // 获取所有已标注的记录
        LambdaQueryWrapper<LabelRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelRecord::getTaskId, taskId)
               .eq(LabelRecord::getStatus, 1);
        List<LabelRecord> allRecords = labelRecordMapper.selectList(wrapper);

        if (allRecords.isEmpty()) {
            return 0.0;
        }

        // 按样本分组
        Map<Long, List<LabelRecord>> bySample = allRecords.stream()
                .collect(Collectors.groupingBy(LabelRecord::getSampleId));

        // H3修复: 根据标注员数量选择Cohen's Kappa或Fleiss' Kappa
        int annotatorCount = task.getAnnotatorCount() != null ? task.getAnnotatorCount() : 2;
        double totalKappa = 0.0;
        int count = 0;

        for (List<LabelRecord> records : bySample.values()) {
            if (records.size() < 2) {
                continue;
            }

            double kappa;
            if (annotatorCount <= 2) {
                // 2名标注员: 使用Cohen's Kappa
                String[] labels1 = parseAnnotations(records.get(0).getAnnotations());
                String[] labels2 = parseAnnotations(records.get(1).getAnnotations());
                if (labels1.length > 0 && labels2.length > 0) {
                    kappa = IaaEngine.calculateCohenKappa(labels1, labels2);
                    totalKappa += kappa;
                    count++;
                }
            } else {
                // 3名及以上标注员: 使用Fleiss' Kappa
                kappa = calculateFleissKappaForSample(records);
                totalKappa += kappa;
                count++;
            }
        }

        double avgKappa = count > 0 ? totalKappa / count : 0.0;
        task.setKappaScore(avgKappa);
        labelTaskMapper.updateById(task);

        return avgKappa;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void triggerRelabel(Long taskId) {
        LabelTask task = getTaskById(taskId);
        log.warn("触发整体重标, 任务ID: {}", taskId);

        // 删除所有标注记录
        LambdaQueryWrapper<LabelRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelRecord::getTaskId, taskId);
        labelRecordMapper.delete(wrapper);

        // 重新创建标注记录
        try {
            List<Long> annotatorIds = objectMapper.readValue(task.getAnnotatorIds(), new TypeReference<List<Long>>() {});
            LambdaQueryWrapper<DataSample> sampleWrapper = new LambdaQueryWrapper<>();
            sampleWrapper.eq(DataSample::getDatasetId, task.getDatasetId());
            List<DataSample> samples = dataSampleMapper.selectList(sampleWrapper);

            for (DataSample sample : samples) {
                for (Long annotatorId : annotatorIds) {
                    LabelRecord record = new LabelRecord();
                    record.setTaskId(taskId);
                    record.setSampleId(sample.getId());
                    record.setAnnotatorId(annotatorId);
                    record.setStatus(0);
                    labelRecordMapper.insert(record);
                }
            }
        } catch (Exception e) {
            log.error("重新创建标注记录失败", e);
        }

        task.setStatus(LabelTaskStatus.IN_PROGRESS.getCode());
        task.setLabeledCount(0);
        task.setKappaScore(null);
        labelTaskMapper.updateById(task);
    }
}
