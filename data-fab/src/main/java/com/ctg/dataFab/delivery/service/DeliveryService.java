package com.ctg.dataFab.delivery.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.delivery.dto.PublishRequest;
import com.ctg.dataFab.delivery.dto.PublishResponse;
import com.ctg.dataFab.delivery.entity.DeliveryRecord;

/**
 * 数据集交付服务接口
 *
 * @author Developer
 * @since 2026-07-01
 */
public interface DeliveryService {

    /**
     * 发布数据集
     */
    PublishResponse publish(PublishRequest request);

    /**
     * 获取交付记录
     */
    DeliveryRecord getRecordById(Long id);

    /**
     * 分页查询交付记录
     */
    Page<DeliveryRecord> listRecords(Long datasetId, Integer status, PageRequest pageRequest);

    /**
     * 生成下载令牌
     */
    String generateDownloadToken(Long recordId, Long userId, String approvalId);

    /**
     * 下架数据集
     */
    void unpublish(Long recordId);
}
