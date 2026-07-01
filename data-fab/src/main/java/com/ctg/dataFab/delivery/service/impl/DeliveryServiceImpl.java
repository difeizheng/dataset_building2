package com.ctg.dataFab.delivery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.common.enums.DataStatus;
import com.ctg.dataFab.common.exception.BusinessException;
import com.ctg.dataFab.common.util.Utils;
import com.ctg.dataFab.delivery.dto.PublishRequest;
import com.ctg.dataFab.delivery.dto.PublishResponse;
import com.ctg.dataFab.delivery.entity.DeliveryRecord;
import com.ctg.dataFab.delivery.mapper.DeliveryRecordMapper;
import com.ctg.dataFab.delivery.service.DeliveryService;
import com.ctg.dataFab.ingest.entity.Dataset;
import com.ctg.dataFab.ingest.mapper.DatasetMapper;
import com.ctg.dataFab.ingest.service.DatasetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 数据集交付服务实现
 *
 * @author Developer
 * @since 2026-07-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRecordMapper deliveryRecordMapper;
    private final DatasetMapper datasetMapper;
    private final DatasetService datasetService;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PublishResponse publish(PublishRequest request) {
        log.info("发布数据集: 数据集ID={}, 版本={}", request.getDatasetId(), request.getVersion());

        // 检查数据集状态
        Dataset dataset = datasetService.getDatasetById(request.getDatasetId());
        if (dataset.getStatus() != DataStatus.QA_PASS.getCode()) {
            throw new BusinessException("数据集未通过质量评估，无法发布");
        }

        // 创建交付记录
        DeliveryRecord record = new DeliveryRecord();
        record.setDatasetId(request.getDatasetId());
        record.setVersion(request.getVersion());
        record.setDatasetUri("dataset://" + request.getDatasetId() + "/" + request.getVersion());
        record.setLicense(request.getLicense());
        record.setAccessPolicy(request.getAccessPolicy());
        record.setCroissantMetadata(request.getCroissant());
        record.setStatus(1); // 已发布
        record.setDownloadCount(0);

        // 生成数据血缘
        String lineage = String.format("{\"source\":\"%s\",\"version\":\"%s\",\"publishedAt\":\"%s\"}",
                dataset.getName(), request.getVersion(), LocalDateTime.now().toString());
        record.setLineage(lineage);

        deliveryRecordMapper.insert(record);

        // 更新数据集状态为已发布
        dataset.setStatus(DataStatus.PUBLISHED.getCode());
        dataset.setVersion(request.getVersion());
        datasetMapper.updateById(dataset);

        // 构建响应
        PublishResponse response = new PublishResponse();
        response.setRecordId(record.getId());
        response.setDatasetUri(record.getDatasetUri());
        response.setLicense(record.getLicense());
        response.setLineage(record.getLineage());

        log.info("数据集发布成功: 记录ID={}, URI={}", record.getId(), record.getDatasetUri());
        return response;
    }

    @Override
    public DeliveryRecord getRecordById(Long id) {
        DeliveryRecord record = deliveryRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("交付记录不存在");
        }
        return record;
    }

    @Override
    public Page<DeliveryRecord> listRecords(Long datasetId, Integer status, PageRequest pageRequest) {
        LambdaQueryWrapper<DeliveryRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(datasetId != null, DeliveryRecord::getDatasetId, datasetId)
               .eq(status != null, DeliveryRecord::getStatus, status)
               .orderByDesc(DeliveryRecord::getCreateTime);

        Page<DeliveryRecord> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        return deliveryRecordMapper.selectPage(page, wrapper);
    }

    @Override
    public String generateDownloadToken(Long recordId, String approvalId) {
        DeliveryRecord record = getRecordById(recordId);
        if (record.getStatus() != 1) {
            throw new BusinessException("数据集未发布，无法下载");
        }

        // 生成一次性令牌
        String token = Utils.generateUUID();
        String key = "download:token:" + token;
        String value = recordId + ":" + approvalId;

        // 令牌有效期24小时
        redisTemplate.opsForValue().set(key, value, 24, TimeUnit.HOURS);

        log.info("生成下载令牌: 记录ID={}, 审批单号={}", recordId, approvalId);
        return token;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unpublish(Long recordId) {
        DeliveryRecord record = getRecordById(recordId);
        record.setStatus(2); // 已下架
        deliveryRecordMapper.updateById(record);

        // 更新数据集状态
        Dataset dataset = datasetService.getDatasetById(record.getDatasetId());
        dataset.setStatus(DataStatus.ARCHIVED.getCode());
        datasetMapper.updateById(dataset);

        log.info("数据集下架成功: 记录ID={}", recordId);
    }
}
