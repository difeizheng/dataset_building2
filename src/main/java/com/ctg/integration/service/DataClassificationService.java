package com.ctg.integration.service;

import java.util.List;

import com.ctg.integration.dto.classification.*;

/**
 * 数据分级服务接口
 *
 * @author CTG
 * @since 2026-07-01
 */
public interface DataClassificationService {

    /**
     * 创建数据分级
     */
    DataClassificationVO createClassification(CreateClassificationRequest request);

    /**
     * 更新数据分级
     */
    DataClassificationVO updateClassification(Long id, UpdateClassificationRequest request);

    /**
     * 删除数据分级
     */
    void deleteClassification(Long id);

    /**
     * 获取数据分级详情
     */
    DataClassificationVO getClassificationById(Long id);

    /**
     * 根据级别获取数据分级
     */
    DataClassificationVO getClassificationByLevel(String level);

    /**
     * 获取所有数据分级
     */
    List<DataClassificationVO> listClassifications();

    /**
     * 检查数据是否需要加密
     */
    boolean requiresEncryption(String securityLevel);

    /**
     * 检查数据是否需要脱敏
     */
    boolean requiresMasking(String securityLevel);
}
