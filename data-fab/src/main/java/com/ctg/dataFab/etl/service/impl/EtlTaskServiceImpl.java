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
import com.ctg.dataFab.etl.service.EtlTaskService;
import com.ctg.dataFab.ingest.entity.DataSample;
import com.ctg.dataFab.ingest.mapper.DataSampleMapper;
import com.ctg.dataFab.ingest.service.DatasetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 清洗任务服务实现
 *
 * @author Developer
 * @since 2026-07-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EtlTaskServiceImpl implements EtlTaskService {

    private final EtlTaskMapper etlTaskMapper;
    private final EtlRuleMapper etlRuleMapper;
    private final DataSampleMapper dataSampleMapper;
    private final DatasetService datasetService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTask(Long datasetId, String taskName) {
        log.info("创建清洗任务: 数据集ID={}, 任务名={}", datasetId, taskName);

        EtlTask task = new EtlTask();
        task.setDatasetId(datasetId);
        task.setTaskName(taskName);
        task.setStatus(0); // 待执行
        task.setProcessedCount(0);
        task.setSuccessCount(0);
        task.setFailedCount(0);

        etlTaskMapper.insert(task);
        log.info("清洗任务创建成功, ID: {}", task.getId());
        return task.getId();
    }

    @Override
    @Async
    public void executeTask(Long taskId) {
        EtlTask task = getTaskById(taskId);
        if (task.getStatus() != 0) {
            throw new BusinessException("任务状态不正确，无法执行");
        }

        log.info("开始执行清洗任务, ID: {}", taskId);
        task.setStatus(1); // 执行中
        task.setStartTime(LocalDateTime.now());
        etlTaskMapper.updateById(task);

        try {
            // 获取数据集的所有样本
            LambdaQueryWrapper<DataSample> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DataSample::getDatasetId, task.getDatasetId());
            List<DataSample> samples = dataSampleMapper.selectList(wrapper);

            // 获取适用的清洗规则
            LambdaQueryWrapper<EtlRule> ruleWrapper = new LambdaQueryWrapper<>();
            ruleWrapper.eq(EtlRule::getEnabled, 1)
                      .orderByDesc(EtlRule::getPriority);
            List<EtlRule> rules = etlRuleMapper.selectList(ruleWrapper);

            int processed = 0;
            int success = 0;
            int failed = 0;

            // 对每个样本应用清洗规则
            for (DataSample sample : samples) {
                try {
                    // TODO: 实现具体的清洗逻辑
                    // 这里应该根据规则类型执行相应的清洗操作
                    // 例如：去重、标准化、脱敏、分级等

                    // 更新样本状态为已清洗
                    sample.setStatus(DataStatus.CLEAN.getCode());
                    dataSampleMapper.updateById(sample);

                    success++;
                } catch (Exception e) {
                    log.error("清洗样本失败, 样本ID: {}", sample.getId(), e);
                    failed++;
                }
                processed++;

                // 更新任务进度
                task.setProcessedCount(processed);
                task.setSuccessCount(success);
                task.setFailedCount(failed);
                etlTaskMapper.updateById(task);
            }

            task.setStatus(2); // 已完成
            task.setEndTime(LocalDateTime.now());
            etlTaskMapper.updateById(task);

            // 更新数据集统计信息
            datasetService.updateDatasetStats(task.getDatasetId());

            log.info("清洗任务执行完成, ID: {}, 处理: {}, 成功: {}, 失败: {}",
                    taskId, processed, success, failed);

        } catch (Exception e) {
            log.error("清洗任务执行失败, ID: {}", taskId, e);
            task.setStatus(3); // 失败
            task.setEndTime(LocalDateTime.now());
            task.setErrorMessage(e.getMessage());
            etlTaskMapper.updateById(task);
        }
    }

    @Override
    public EtlTask getTaskById(Long id) {
        EtlTask task = etlTaskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("清洗任务不存在");
        }
        return task;
    }

    @Override
    public Page<EtlTask> listTasks(Long datasetId, Integer status, PageRequest pageRequest) {
        LambdaQueryWrapper<EtlTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(datasetId != null, EtlTask::getDatasetId, datasetId)
               .eq(status != null, EtlTask::getStatus, status)
               .orderByDesc(EtlTask::getCreateTime);

        Page<EtlTask> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        return etlTaskMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTask(Long taskId) {
        EtlTask task = getTaskById(taskId);
        if (task.getStatus() == 1) { // 执行中
            task.setStatus(3); // 标记为失败
            task.setEndTime(LocalDateTime.now());
            task.setErrorMessage("任务被取消");
            etlTaskMapper.updateById(task);
            log.info("清洗任务已取消, ID: {}", taskId);
        } else {
            throw new BusinessException("只能取消执行中的任务");
        }
    }
}
