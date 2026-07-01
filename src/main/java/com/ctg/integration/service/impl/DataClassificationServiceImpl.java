package com.ctg.integration.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ctg.integration.dto.classification.*;
import com.ctg.integration.entity.DataClassification;
import com.ctg.integration.exception.BusinessException;
import com.ctg.integration.repository.DataClassificationRepository;
import com.ctg.integration.service.DataClassificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 数据分级服务实现
 *
 * @author CTG
 * @since 2026-07-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataClassificationServiceImpl implements DataClassificationService {

    private final DataClassificationRepository classificationRepository;

    @Override
    @Transactional
    public DataClassificationVO createClassification(CreateClassificationRequest request) {
        log.info("创建数据分级: level={}", request.getSecurityLevel());

        // 检查级别是否已存在
        if (classificationRepository.existsBySecurityLevel(request.getSecurityLevel())) {
            throw new BusinessException("LEVEL_EXISTS", "安全级别已存在: " + request.getSecurityLevel());
        }

        DataClassification classification = DataClassification.builder()
                .classificationName(request.getClassificationName())
                .securityLevel(request.getSecurityLevel())
                .requireEncryption(request.getRequireEncryption() != null ? request.getRequireEncryption() : false)
                .requireMasking(request.getRequireMasking() != null ? request.getRequireMasking() : false)
                .maskingRule(request.getMaskingRule())
                .accessPolicy(request.getAccessPolicy())
                .description(request.getDescription())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        classification = classificationRepository.save(classification);
        return toVO(classification);
    }

    @Override
    @Transactional
    public DataClassificationVO updateClassification(Long id, UpdateClassificationRequest request) {
        log.info("更新数据分级: id={}", id);

        DataClassification classification = classificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("CLASSIFICATION_NOT_FOUND", "数据分级不存在: " + id));

        if (request.getClassificationName() != null) {
            classification.setClassificationName(request.getClassificationName());
        }
        if (request.getRequireEncryption() != null) {
            classification.setRequireEncryption(request.getRequireEncryption());
        }
        if (request.getRequireMasking() != null) {
            classification.setRequireMasking(request.getRequireMasking());
        }
        if (request.getMaskingRule() != null) {
            classification.setMaskingRule(request.getMaskingRule());
        }
        if (request.getAccessPolicy() != null) {
            classification.setAccessPolicy(request.getAccessPolicy());
        }
        if (request.getDescription() != null) {
            classification.setDescription(request.getDescription());
        }

        classification.setUpdatedAt(LocalDateTime.now());
        classification = classificationRepository.save(classification);
        return toVO(classification);
    }

    @Override
    @Transactional
    public void deleteClassification(Long id) {
        log.info("删除数据分级: id={}", id);

        DataClassification classification = classificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("CLASSIFICATION_NOT_FOUND", "数据分级不存在: " + id));

        classificationRepository.delete(classification);
    }

    @Override
    @Transactional(readOnly = true)
    public DataClassificationVO getClassificationById(Long id) {
        DataClassification classification = classificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("CLASSIFICATION_NOT_FOUND", "数据分级不存在: " + id));
        return toVO(classification);
    }

    @Override
    @Transactional(readOnly = true)
    public DataClassificationVO getClassificationByLevel(String level) {
        DataClassification classification = classificationRepository.findBySecurityLevel(level)
                .orElseThrow(() -> new BusinessException("LEVEL_NOT_FOUND", "安全级别不存在: " + level));
        return toVO(classification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DataClassificationVO> listClassifications() {
        List<DataClassification> classifications = classificationRepository.findAll();
        return classifications.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public boolean requiresEncryption(String securityLevel) {
        return classificationRepository.findBySecurityLevel(securityLevel)
                .map(DataClassification::getRequireEncryption)
                .orElse(false);
    }

    @Override
    public boolean requiresMasking(String securityLevel) {
        return classificationRepository.findBySecurityLevel(securityLevel)
                .map(DataClassification::getRequireMasking)
                .orElse(false);
    }

    private DataClassificationVO toVO(DataClassification entity) {
        return DataClassificationVO.builder()
                .id(entity.getId())
                .classificationName(entity.getClassificationName())
                .securityLevel(entity.getSecurityLevel())
                .requireEncryption(entity.getRequireEncryption())
                .requireMasking(entity.getRequireMasking())
                .maskingRule(entity.getMaskingRule())
                .accessPolicy(entity.getAccessPolicy())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
