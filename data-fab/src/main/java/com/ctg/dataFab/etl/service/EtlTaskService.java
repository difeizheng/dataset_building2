package com.ctg.dataFab.etl.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.etl.entity.EtlTask;

/**
 * 清洗任务服务接口
 *
 * @author Developer
 * @since 2026-07-01
 */
public interface EtlTaskService {

    /**
     * 创建清洗任务
     */
    Long createTask(Long datasetId, String taskName);

    /**
     * 执行清洗任务
     */
    void executeTask(Long taskId);

    /**
     * 根据ID获取清洗任务
     */
    EtlTask getTaskById(Long id);

    /**
     * 分页查询清洗任务
     */
    Page<EtlTask> listTasks(Long datasetId, Integer status, PageRequest pageRequest);

    /**
     * 取消清洗任务
     */
    void cancelTask(Long taskId);
}
