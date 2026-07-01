package com.ctg.dataFab.access.service;

import com.ctg.dataFab.classification.engine.DataClassificationEngine;
import com.ctg.dataFab.classification.engine.DataClassificationEngine.ClassificationResult;
import com.ctg.dataFab.common.enums.DataLevel;
import com.ctg.dataFab.common.exception.BusinessException;
import com.ctg.dataFab.ingest.entity.DataSample;
import com.ctg.dataFab.ingest.mapper.DataSampleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 数据访问控制服务
 * 实现 L4 核心数据的下载/打印/外发阻断
 *
 * @author Developer
 * @since 2026-07-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataAccessControlService {

    private final DataSampleMapper dataSampleMapper;

    /**
     * 检查是否允许下载
     *
     * @param sampleId 数据样本ID
     * @param userId 用户ID
     * @return 是否允许
     */
    public boolean canDownload(Long sampleId, Long userId) {
        DataSample sample = dataSampleMapper.selectById(sampleId);
        if (sample == null) {
            throw new BusinessException("数据样本不存在");
        }

        DataLevel level = DataLevel.fromCode(sample.getDataLevel());

        // L4 核心数据禁止下载
        if (level == DataLevel.L4_CORE) {
            log.warn("L4核心数据禁止下载: sampleId={}, userId={}", sampleId, userId);
            auditAccess(sampleId, userId, "DOWNLOAD_BLOCKED", "L4核心数据禁止下载");
            return false;
        }

        // L3 敏感数据需要审批
        if (level == DataLevel.L3_SENSITIVE) {
            boolean approved = checkApproval(sampleId, userId, "DOWNLOAD");
            if (!approved) {
                log.warn("L3敏感数据下载需要审批: sampleId={}, userId={}", sampleId, userId);
                auditAccess(sampleId, userId, "DOWNLOAD_PENDING_APPROVAL", "L3敏感数据下载待审批");
                return false;
            }
        }

        auditAccess(sampleId, userId, "DOWNLOAD_ALLOWED", "允许下载");
        return true;
    }

    /**
     * 检查是否允许打印
     *
     * @param sampleId 数据样本ID
     * @param userId 用户ID
     * @return 是否允许
     */
    public boolean canPrint(Long sampleId, Long userId) {
        DataSample sample = dataSampleMapper.selectById(sampleId);
        if (sample == null) {
            throw new BusinessException("数据样本不存在");
        }

        DataLevel level = DataLevel.fromCode(sample.getDataLevel());

        // L4 核心数据禁止打印
        if (level == DataLevel.L4_CORE) {
            log.warn("L4核心数据禁止打印: sampleId={}, userId={}", sampleId, userId);
            auditAccess(sampleId, userId, "PRINT_BLOCKED", "L4核心数据禁止打印");
            return false;
        }

        auditAccess(sampleId, userId, "PRINT_ALLOWED", "允许打印");
        return true;
    }

    /**
     * 检查是否允许外发
     *
     * @param sampleId 数据样本ID
     * @param userId 用户ID
     * @return 是否允许
     */
    public boolean canExport(Long sampleId, Long userId) {
        DataSample sample = dataSampleMapper.selectById(sampleId);
        if (sample == null) {
            throw new BusinessException("数据样本不存在");
        }

        DataLevel level = DataLevel.fromCode(sample.getDataLevel());

        // L4 核心数据禁止外发
        if (level == DataLevel.L4_CORE) {
            log.warn("L4核心数据禁止外发: sampleId={}, userId={}", sampleId, userId);
            auditAccess(sampleId, userId, "EXPORT_BLOCKED", "L4核心数据禁止外发");
            return false;
        }

        // L3 敏感数据需要审批
        if (level == DataLevel.L3_SENSITIVE) {
            boolean approved = checkApproval(sampleId, userId, "EXPORT");
            if (!approved) {
                log.warn("L3敏感数据外发需要审批: sampleId={}, userId={}", sampleId, userId);
                auditAccess(sampleId, userId, "EXPORT_PENDING_APPROVAL", "L3敏感数据外发待审批");
                return false;
            }
        }

        auditAccess(sampleId, userId, "EXPORT_ALLOWED", "允许外发");
        return true;
    }

    /**
     * 检查是否允许在线查看
     *
     * @param sampleId 数据样本ID
     * @param userId 用户ID
     * @return 是否允许
     */
    public boolean canView(Long sampleId, Long userId) {
        DataSample sample = dataSampleMapper.selectById(sampleId);
        if (sample == null) {
            throw new BusinessException("数据样本不存在");
        }

        DataLevel level = DataLevel.fromCode(sample.getDataLevel());

        // L4 核心数据仅允许在线查看（带水印）
        if (level == DataLevel.L4_CORE) {
            log.info("L4核心数据允许在线查看（带水印）: sampleId={}, userId={}", sampleId, userId);
            auditAccess(sampleId, userId, "VIEW_ALLOWED_WITH_WATERMARK", "L4核心数据允许在线查看（带水印）");
            return true;
        }

        auditAccess(sampleId, userId, "VIEW_ALLOWED", "允许查看");
        return true;
    }

    /**
     * 检查是否需要水印
     *
     * @param sampleId 数据样本ID
     * @return 是否需要水印
     */
    public boolean requiresWatermark(Long sampleId) {
        DataSample sample = dataSampleMapper.selectById(sampleId);
        if (sample == null) {
            throw new BusinessException("数据样本不存在");
        }

        DataLevel level = DataLevel.fromCode(sample.getDataLevel());
        return level == DataLevel.L4_CORE || level == DataLevel.L3_SENSITIVE;
    }

    /**
     * 检查审批状态
     */
    private boolean checkApproval(Long sampleId, Long userId, String action) {
        // TODO: 实现审批流程检查
        // 这里应该查询审批表，检查是否有有效的审批记录
        log.info("检查审批状态: sampleId={}, userId={}, action={}", sampleId, userId, action);
        return false; // 默认需要审批
    }

    /**
     * 审计访问记录
     */
    private void auditAccess(Long sampleId, Long userId, String action, String description) {
        // TODO: 实现审计日志记录
        log.info("审计访问记录: sampleId={}, userId={}, action={}, description={}",
                sampleId, userId, action, description);
    }
}
