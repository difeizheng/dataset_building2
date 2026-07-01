package com.ctg.dataFab.ingest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.common.enums.DataStatus;
import com.ctg.dataFab.common.exception.BusinessException;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDataSample(DataSampleCreateRequest request) {
        log.info("创建数据样本: {}", request.getName());

        DataSample dataSample = new DataSample();
        dataSample.setName(request.getName());
        dataSample.setModality(request.getModality());
        dataSample.setDataLevel(request.getDataLevel());
        dataSample.setStatus(DataStatus.NEW.getCode());
        dataSample.setSource(request.getSource());
        dataSample.setFilePath(request.getFilePath());
        dataSample.setFileSize(request.getFileSize());
        dataSample.setMimeType(request.getMimeType());
        dataSample.setFileHash(request.getFileHash());
        dataSample.setMetadata(request.getMetadata());

        dataSampleMapper.insert(dataSample);

        // 更新数据集统计信息
        if (dataSample.getDatasetId() != null) {
            datasetService.updateDatasetStats(dataSample.getDatasetId());
        }

        log.info("数据样本创建成功, ID: {}", dataSample.getId());
        return dataSample.getId();
    }

    @Override
    public DataSample getDataSampleById(Long id) {
        DataSample dataSample = dataSampleMapper.selectById(id);
        if (dataSample == null) {
            throw new BusinessException("数据样本不存在");
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
        DataSample dataSample = getDataSampleById(id);
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
