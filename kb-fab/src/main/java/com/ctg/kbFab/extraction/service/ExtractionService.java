package com.ctg.kbFab.extraction.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.kbFab.extraction.dto.ExtractionRequest;
import com.ctg.kbFab.extraction.dto.ExtractionResult;
import com.ctg.kbFab.extraction.entity.ExtractionTask;

/**
 * 知识抽取服务接口
 *
 * @author Developer
 * @since 2026-07-01
 */
public interface ExtractionService {

    /**
     * 创建抽取任务
     */
    ExtractionTask createTask(ExtractionRequest request);

    /**
     * 执行抽取任务
     */
    ExtractionResult executeTask(String taskId);

    /**
     * 获取任务详情
     */
    ExtractionTask getTask(String taskId);

    /**
     * 分页查询任务
     */
    Page<ExtractionTask> listTasks(Integer page, Integer size);

    /**
     * 删除任务
     */
    void deleteTask(String taskId);
}
