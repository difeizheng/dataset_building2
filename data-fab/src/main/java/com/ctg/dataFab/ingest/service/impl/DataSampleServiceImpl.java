package com.ctg.dataFab.ingest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.dataFab.classification.engine.DataClassificationEngine;
import com.ctg.dataFab.classification.engine.DataClassificationEngine.ClassificationContext;
import com.ctg.dataFab.classification.engine.DataClassificationEngine.ClassificationResult;
import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.common.enums.DataStatus;
import com.ctg.dataFab.common.exception.BusinessException;
import com.ctg.dataFab.crypto.Sm4Service;
import com.ctg.dataFab.ingest.dto.DataSampleCreateRequest;
import com.ctg.dataFab.ingest.entity.DataSample;
import com.ctg.dataFab.ingest.mapper.DataSampleMapper;
import com.ctg.dataFab.ingest.service.DataSampleService;
import com.ctg.dataFab.ingest.service.DatasetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 数据样本服务实现
 *
 * @author Developer
 * @since 2026-07-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataSampleServiceImpl implements DataSampleService {

    private final DataSampleMapper dataSampleMapper;
    private final DatasetService datasetService;
    private final Sm4Service sm4Service;

    /** L4 核心数据分级编码 */
    private static final int DATA_LEVEL_L4_CORE = 4;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDataSample(DataSampleCreateRequest request) {
        log.info("创建数据样本: {}", request.getName());

        DataSample dataSample = new DataSample();
        dataSample.setName(request.getName());
        dataSample.setModality(request.getModality());

        // C1: 自动分级 - 如果未指定分级，使用分级引擎自动判定
        Integer dataLevel = request.getDataLevel();
        if (dataLevel == null) {
            ClassificationContext context = new ClassificationContext(
                    request.getName(),
                    null,
                    request.getMimeType()
            );
            context.setTags(request.getMetadata());
            context.setMetadata(request.getMetadata());

            ClassificationResult classificationResult = DataClassificationEngine.classify(context);
            dataLevel = classificationResult.getLevel().getCode();
            log.info("自动分级结果: level={}, reason={}",
                    classificationResult.getLevel().getName(),
                    classificationResult.getReason());
        }
        dataSample.setDataLevel(dataLevel);

        dataSample.setStatus(DataStatus.NEW.getCode());
        dataSample.setSource(request.getSource());
        dataSample.setFilePath(request.getFilePath());
        dataSample.setFileSize(request.getFileSize());
        dataSample.setMimeType(request.getMimeType());
        dataSample.setFileHash(request.getFileHash());
        dataSample.setMetadata(request.getMetadata());

        // L4 核心数据：SM4 加密落盘
        if (dataLevel == DATA_LEVEL_L4_CORE) {
            log.info("L4 核心数据，启用 SM4 加密落盘");
            if (dataSample.getFilePath() != null && !dataSample.getFilePath().isBlank()) {
                dataSample.setFilePath(sm4Service.encrypt(dataSample.getFilePath()));
            }
            if (dataSample.getMetadata() != null && !dataSample.getMetadata().isBlank()) {
                dataSample.setMetadata(sm4Service.encrypt(dataSample.getMetadata()));
            }
        }

        dataSampleMapper.insert(dataSample);

        // 更新数据集统计信息
        if (dataSample.getDatasetId() != null) {
            datasetService.updateDatasetStats(dataSample.getDatasetId());
        }

        log.info("数据样本创建成功, ID: {}, 分级: L{}", dataSample.getId(), dataLevel);
        return dataSample.getId();
    }

    @Override
    public DataSample getDataSampleById(Long id) {
        DataSample dataSample = dataSampleMapper.selectById(id);
        if (dataSample == null) {
            throw new BusinessException("数据样本不存在");
        }

        // L4 核心数据：SM4 解密
        if (dataSample.getDataLevel() != null && dataSample.getDataLevel() == DATA_LEVEL_L4_CORE) {
            log.debug("L4 核心数据，启用 SM4 解密");
            if (dataSample.getFilePath() != null && !dataSample.getFilePath().isBlank()) {
                dataSample.setFilePath(sm4Service.decrypt(dataSample.getFilePath()));
            }
            if (dataSample.getMetadata() != null && !dataSample.getMetadata().isBlank()) {
                dataSample.setMetadata(sm4Service.decrypt(dataSample.getMetadata()));
            }
        }

        return dataSample;
    }

    @Override
    public Page<DataSample> listDataSamples(Long datasetId, Integer modality, Integer dataLevel,
                                            Integer status, PageRequest pageRequest) {
        LambdaQueryWrapper<DataSample> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(datasetId != null, DataSample::getDatasetId, datasetId)
               .eq(modality != null, DataSample::getModality, modality)
               .eq(dataLevel != null, DataSample::getDataLevel, dataLevel)
               .eq(status != null, DataSample::getStatus, status)
               .orderByDesc(DataSample::getCreateTime);

        Page<DataSample> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        return dataSampleMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDataSampleStatus(Long id, Integer status) {
        // 直接查库，避免 getDataSampleById 对 L4 数据先解密再回写导致双重解密
        DataSample dataSample = dataSampleMapper.selectById(id);
        if (dataSample == null) {
            throw new BusinessException("数据样本不存在");
        }
        dataSample.setStatus(status);
        dataSampleMapper.updateById(dataSample);
        log.info("数据样本状态更新成功, ID: {}, 新状态: {}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDataSample(Long id) {
        DataSample dataSample = getDataSampleById(id);
        dataSampleMapper.deleteById(id);
        log.info("数据样本删除成功, ID: {}", id);

        // 更新数据集统计信息
        if (dataSample.getDatasetId() != null) {
            datasetService.updateDatasetStats(dataSample.getDatasetId());
        }
    }
}
