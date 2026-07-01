package com.ctg.dataFab.ingest.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.ingest.dto.DatasetCreateRequest;
import com.ctg.dataFab.ingest.entity.Dataset;

/**
 * 数据集服务接口
 *
 * @author Developer
 * @since 2026-07-01
 */
public interface DatasetService {

    /**
     * 创建数据集
     *
     * @param request 创建请求
     * @return 数据集ID
     */
    Long createDataset(DatasetCreateRequest request);

    /**
     * 根据ID获取数据集
     *
     * @param id 数据集ID
     * @return 数据集
     */
    Dataset getDatasetById(Long id);

    /**
     * 分页查询数据集
     *
     * @param modality 数据模态（可选）
     * @param dataLevel 数据分级（可选）
     * @param status 数据状态（可选）
     * @param pageRequest 分页请求
     * @return 分页结果
     */
    Page<Dataset> listDatasets(Integer modality, Integer dataLevel, Integer status, PageRequest pageRequest);

    /**
     * 更新数据集
     *
     * @param dataset 数据集
     */
    void updateDataset(Dataset dataset);

    /**
     * 删除数据集
     *
     * @param id 数据集ID
     */
    void deleteDataset(Long id);

    /**
     * 更新数据集样本数量和总大小
     *
     * @param datasetId 数据集ID
     */
    void updateDatasetStats(Long datasetId);
}
