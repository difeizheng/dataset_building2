package com.ctg.kbFab.extraction.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.kbFab.common.enums.ExtractionStatus;
import com.ctg.kbFab.common.exception.BusinessException;
import com.ctg.kbFab.extraction.dto.ExtractionRequest;
import com.ctg.kbFab.extraction.dto.ExtractionResult;
import com.ctg.kbFab.extraction.engine.ExtractionEngine;
import com.ctg.kbFab.extraction.entity.ExtractionTask;
import com.ctg.kbFab.extraction.mapper.ExtractionTaskMapper;
import com.ctg.kbFab.extraction.service.ExtractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 知识抽取服务实现
 *
 * @author Developer
 * @since 2026-07-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExtractionServiceImpl implements ExtractionService {

    private final ExtractionTaskMapper extractionTaskMapper;
    private final ExtractionEngine extractionEngine;

    @Override
    @Transactional
    public ExtractionTask createTask(ExtractionRequest request) {
        ExtractionTask task = new ExtractionTask();
        task.setTaskName(request.getTaskName());
        task.setInputText(request.getInputText());
        task.setExtractionType(request.getExtractionType());
        task.setStatus(ExtractionStatus.PENDING);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        task.setDeleted(0);

        extractionTaskMapper.insert(task);
        log.info("Created extraction task: {}", task.getId());
        return task;
    }

    @Override
    @Transactional
    public ExtractionResult executeTask(String taskId) {
        ExtractionTask task = getTask(taskId);
        if (task == null) {
            throw new BusinessException("Task not found: " + taskId);
        }

        long startTime = System.currentTimeMillis();
        task.setStatus(ExtractionStatus.PROCESSING);
        task.setUpdateTime(LocalDateTime.now());
        extractionTaskMapper.updateById(task);

        try {
            ExtractionResult result = new ExtractionResult();
            result.setTaskId(taskId);
            result.setExtractionType(task.getExtractionType());

            // 根据抽取类型执行相应的抽取逻辑
            switch (task.getExtractionType().toUpperCase()) {
                case "NER":
                    result.setEntities(extractionEngine.extractEntities(task.getInputText()));
                    break;
                case "RELATION":
                    var entities = extractionEngine.extractEntities(task.getInputText());
                    result.setEntities(entities);
                    result.setRelations(extractionEngine.extractRelations(task.getInputText(), entities));
                    break;
                case "ATTRIBUTE":
                    var attrs = extractionEngine.extractEntities(task.getInputText());
                    result.setEntities(attrs);
                    result.setAttributes(extractionEngine.extractAttributes(task.getInputText(), attrs));
                    break;
                case "EVENT":
                    result.setEvents(extractionEngine.extractEvents(task.getInputText()));
                    break;
                default:
                    throw new BusinessException("Unsupported extraction type: " + task.getExtractionType());
            }

            long duration = System.currentTimeMillis() - startTime;
            task.setStatus(ExtractionStatus.COMPLETED);
            task.setDurationMs(duration);
            task.setUpdateTime(LocalDateTime.now());
            extractionTaskMapper.updateById(task);

            log.info("Completed extraction task {} in {}ms", taskId, duration);
            return result;

        } catch (Exception e) {
            log.error("Failed to execute extraction task: {}", taskId, e);
            task.setStatus(ExtractionStatus.FAILED);
            task.setErrorMessage(e.getMessage());
            task.setUpdateTime(LocalDateTime.now());
            extractionTaskMapper.updateById(task);
            throw new BusinessException("Extraction failed: " + e.getMessage());
        }
    }

    @Override
    public ExtractionTask getTask(String taskId) {
        return extractionTaskMapper.selectById(taskId);
    }

    @Override
    public Page<ExtractionTask> listTasks(Integer page, Integer size) {
        Page<ExtractionTask> pageParam = new Page<>(page, size);
        return extractionTaskMapper.selectPage(pageParam, null);
    }

    @Override
    @Transactional
    public void deleteTask(String taskId) {
        ExtractionTask task = getTask(taskId);
        if (task == null) {
            throw new BusinessException("Task not found: " + taskId);
        }
        extractionTaskMapper.deleteById(taskId);
        log.info("Deleted extraction task: {}", taskId);
    }
}
