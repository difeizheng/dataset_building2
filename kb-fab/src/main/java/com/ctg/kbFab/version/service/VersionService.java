package com.ctg.kbFab.version.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ctg.kbFab.common.exception.BusinessException;
import com.ctg.kbFab.storage.entity.KnowledgeEntry;
import com.ctg.kbFab.storage.mapper.KnowledgeEntryMapper;
import com.ctg.kbFab.version.entity.KnowledgeVersion;
import com.ctg.kbFab.version.mapper.KnowledgeVersionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VersionService {

    private final KnowledgeVersionMapper versionMapper;
    private final KnowledgeEntryMapper knowledgeMapper;

    @Transactional
    public KnowledgeVersion createVersion(String knowledgeId, String changeNote, String operator, String operationType) {
        KnowledgeEntry entry = knowledgeMapper.selectById(knowledgeId);
        if (entry == null) {
            throw new BusinessException("Knowledge entry not found: " + knowledgeId);
        }

        KnowledgeVersion version = new KnowledgeVersion();
        version.setKnowledgeId(knowledgeId);
        version.setVersionNumber(entry.getVersion());
        version.setTitle(entry.getTitle());
        version.setContentSnapshot(entry.getContent());
        version.setChangeNote(changeNote);
        version.setOperator(operator != null ? operator : "system");
        version.setOperationType(operationType);
        version.setCreateTime(LocalDateTime.now());
        version.setDeleted(0);

        versionMapper.insert(version);
        log.info("Created version {} for knowledge {}: {}", version.getVersionNumber(), knowledgeId, operationType);
        return version;
    }

    public List<KnowledgeVersion> listVersions(String knowledgeId) {
        LambdaQueryWrapper<KnowledgeVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeVersion::getKnowledgeId, knowledgeId)
                .orderByDesc(KnowledgeVersion::getVersionNumber);
        return versionMapper.selectList(wrapper);
    }

    public KnowledgeVersion getVersion(String versionId) {
        return versionMapper.selectById(versionId);
    }

    @Transactional
    public KnowledgeEntry rollback(String knowledgeId, Integer targetVersion) {
        KnowledgeEntry entry = knowledgeMapper.selectById(knowledgeId);
        if (entry == null) {
            throw new BusinessException("Knowledge entry not found: " + knowledgeId);
        }

        LambdaQueryWrapper<KnowledgeVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeVersion::getKnowledgeId, knowledgeId)
                .eq(KnowledgeVersion::getVersionNumber, targetVersion);
        KnowledgeVersion target = versionMapper.selectOne(wrapper);
        if (target == null) {
            throw new BusinessException("Version not found: " + targetVersion);
        }

        entry.setTitle(target.getTitle());
        entry.setContent(target.getContentSnapshot());
        entry.setVersion(entry.getVersion() + 1);
        entry.setUpdateTime(LocalDateTime.now());
        knowledgeMapper.updateById(entry);

        createVersion(knowledgeId, "Rolled back to version " + targetVersion, "system", "ROLLBACK");
        log.info("Rolled back knowledge {} to version {}", knowledgeId, targetVersion);
        return entry;
    }
}
