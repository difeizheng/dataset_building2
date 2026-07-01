package com.ctg.dataFab.ingest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.common.enums.DataStatus;
import com.ctg.dataFab.common.exception.BusinessException;
import com.ctg.dataFab.ingest.dto.DatasetCreateRequest;
import com.ctg.dataFab.ingest.entity.DataSample;
import com.ctg.dataFab.ingest.entity.Dataset;
import com.ctg.dataFab.ingest.mapper.DataSampleMapper;
import com.ctg.dataFab.ingest.mapper.DatasetMapper;
import com.ctg.dataFab.ingest.service.DatasetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 数据集服务实现
 *
 * @author Developer
 * @since 2026-07-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatasetServiceImpl implements DatasetService {

    private final DatasetMapper datasetMapper;
    private final DataSampleMapper dataSampleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDataset(DatasetCreateRequest request) {
        log.info("创建数据集: {}", request.getName());

        Dataset dataset = new Dataset();
        dataset.setName(request.getName());
        dataset.setDescription(request.getDescription());
        dataset.setModality(request.getModality());
        dataset.setDataLevel(request.getDataLevel());
        dataset.setStatus(DataStatus.NEW.getCode());
        dataset.setVersion(request.getVersion() != null ? request.getVersion() : "1.0.0");
        dataset.setSampleCount(0);
        dataset.setTotalSize(0L);
        dataset.setTags(request.getTags());

        datasetMapper.insert(dataset);

        log.info("数据集创建成功, ID: {}", dataset.getId());
        return dataset.getId();
    }

    @Override
    public Dataset getDatasetById(Long id) {
        Dataset dataset = datasetMapper.selectById(id);
        if (dataset == null) {
            throw new BusinessException("数据集不存在");
        }
        return dataset;
    }

    @Override
    public Page<Dataset> listDatasets(Integer modality, Integer dataLevel, Integer status, PageRequest pageRequest) {
        LambdaQueryWrapper<Dataset> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(modality != null, Dataset::getModality, modality)
               .eq(dataLevel != null, Dataset::getDataLevel, dataLevel)
               .eq(status != null, Dataset::getStatus, status)
               .orderByDesc(Dataset::getCreateTime);

        Page<Dataset> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        return datasetMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDataset(Dataset dataset) {
        datasetMapper.updateById(dataset);
        log.info("数据集更新成功, ID: {}", dataset.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDataset(Long id) {
        Dataset dataset = getDatasetById(id);
        datasetMapper.deleteById(id);
        log.info("数据集删除成功, ID: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDatasetStats(Long datasetId) {
        Dataset dataset = getDatasetById(datasetId);

        // 统计样本数量和总大小
        LambdaQueryWrapper<DataSample> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DataSample::getDatasetId, datasetId);
        Long count = dataSampleMapper.selectCount(wrapper);

        // 计算总大小
        Long totalSize = dataSampleMapper.selectList(wrapper).stream()
                .mapToLong(s -> s.getFileSize() != null ? s.getFileSize() : 0L)
                .sum();

        dataset.setSampleCount(count.intValue());
        dataset.setTotalSize(totalSize);
        datasetMapper.updateById(dataset);

        log.info("数据集统计信息更新成功, ID: {}, 样本数: {}, 总大小: {}", datasetId, count, totalSize);
    }
}
