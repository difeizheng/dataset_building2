package com.ctg.dataFab.qa.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.qa.dto.EvaluateRequest;
import com.ctg.dataFab.qa.dto.EvaluateResponse;
import com.ctg.dataFab.qa.entity.QaTask;

/**
 * 质量评估服务接口
 *
 * @author Developer
 * @since 2026-07-01
 */
public interface QaService {

    /**
     * 执行质量评估
     */
    EvaluateResponse evaluate(EvaluateRequest request);

    /**
     * 获取评估任务详情
     */
    QaTask getTaskById(Long id);

    /**
     * 分页查询评估任务
     */
    Page<QaTask> listTasks(Long datasetId, Integer status, PageRequest pageRequest);

    /**
     * 人工复审
     */
    void manualReview(Long taskId, boolean passed, String comment, String reviewer);

    /**
     * 专家终审
     */
    void expertReview(Long taskId, boolean passed, String comment, String reviewer);

    /**
     * 发布数据集
     */
    void publishDataset(Long taskId);
}
