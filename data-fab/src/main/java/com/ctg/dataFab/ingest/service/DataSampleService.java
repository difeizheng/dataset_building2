package com.ctg.dataFab.ingest.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.ingest.dto.DataSampleCreateRequest;
import com.ctg.dataFab.ingest.entity.DataSample;

/**
 * 数据样本服务接口
 *
 * @author Developer
 * @since 2026-07-01
 */
public interface DataSampleService {

    /**
     * 创建数据样本
     *
     * @param request 创建请求
     * @return 数据样本ID
     */
    Long createDataSample(DataSampleCreateRequest request);

    /**
     * 根据ID获取数据样本
     *
     * @param id 数据样本ID
     * @return 数据样本
     */
    DataSample getDataSampleById(Long id);

    /**
     * 分页查询数据样本
     *
     * @param datasetId 数据集ID（可选）
     * @param modality 数据模态（可选）
     * @param dataLevel 数据分级（可选）
     * @param status 数据状态（可选）
     * @param pageRequest 分页请求
     * @return 分页结果
     */
    Page<DataSample> listDataSamples(Long datasetId, Integer modality, Integer dataLevel,
                                     Integer status, PageRequest pageRequest);

    /**
     * 更新数据样本状态
     *
     * @param id 数据样本ID
     * @param status 新状态
     */
    void updateDataSampleStatus(Long id, Integer status);

    /**
     * 删除数据样本
     *
     * @param id 数据样本ID
     */
    void deleteDataSample(Long id);
}
