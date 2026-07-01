package com.ctg.dataFab.label.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.label.dto.CreateLabelTaskRequest;
import com.ctg.dataFab.label.dto.SubmitLabelRequest;
import com.ctg.dataFab.label.entity.LabelTask;

/**
 * 标注任务服务接口
 *
 * @author Developer
 * @since 2026-07-01
 */
public interface LabelTaskService {

    /**
     * 创建标注任务
     */
    Long createTask(CreateLabelTaskRequest request);

    /**
     * 获取标注任务详情
     */
    LabelTask getTaskById(Long id);

    /**
     * 分页查询标注任务
     */
    Page<LabelTask> listTasks(Long datasetId, Integer status, PageRequest pageRequest);

    /**
     * 提交标注
     */
    void submitLabel(SubmitLabelRequest request);

    /**
     * 触发仲裁
     */
    void triggerArbitration(Long taskId);

    /**
     * 计算并更新IAA得分
     */
    Double calculateIaaScore(Long taskId);

    /**
     * 触发重标（Kappa < 0.70）
     */
    void triggerRelabel(Long taskId);
}
