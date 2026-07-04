package com.ctg.kbFab.access.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ctg.kbFab.access.entity.KnowledgeAccess;
import com.ctg.kbFab.access.mapper.KnowledgeAccessMapper;
import com.ctg.kbFab.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccessService {

    private final KnowledgeAccessMapper accessMapper;

    @Transactional
    public KnowledgeAccess grantAccess(String knowledgeId, String principalId, String principalType,
                                       String accessLevel, LocalDateTime expireTime) {
        KnowledgeAccess access = new KnowledgeAccess();
        access.setKnowledgeId(knowledgeId);
        access.setPrincipalId(principalId);
        access.setPrincipalType(principalType);
        access.setAccessLevel(accessLevel);
        access.setAllowed(true);
        access.setExpireTime(expireTime);
        access.setCreateTime(LocalDateTime.now());
        access.setDeleted(0);

        accessMapper.insert(access);
        log.info("Granted {} access to {} on knowledge {}", accessLevel, principalId, knowledgeId);
        return access;
    }

    @Transactional
    public void revokeAccess(String accessId) {
        KnowledgeAccess access = accessMapper.selectById(accessId);
        if (access == null) {
            throw new BusinessException("Access record not found: " + accessId);
        }
        access.setAllowed(false);
        accessMapper.updateById(access);
        log.info("Revoked access: {}", accessId);
    }

    public boolean checkAccess(String knowledgeId, String principalId, String requiredLevel) {
        LambdaQueryWrapper<KnowledgeAccess> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeAccess::getKnowledgeId, knowledgeId)
                .eq(KnowledgeAccess::getPrincipalId, principalId)
                .eq(KnowledgeAccess::getAllowed, true);

        List<KnowledgeAccess> accesses = accessMapper.selectList(wrapper);

        for (KnowledgeAccess access : accesses) {
            // 检查过期
            if (access.getExpireTime() != null && access.getExpireTime().isBefore(LocalDateTime.now())) {
                continue;
            }
            if (hasRequiredLevel(access.getAccessLevel(), requiredLevel)) {
                return true;
            }
        }
        return false;
    }

    public List<KnowledgeAccess> listAccess(String knowledgeId) {
        LambdaQueryWrapper<KnowledgeAccess> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeAccess::getKnowledgeId, knowledgeId);
        return accessMapper.selectList(wrapper);
    }

    private boolean hasRequiredLevel(String actualLevel, String requiredLevel) {
        int actual = levelToInt(actualLevel);
        int required = levelToInt(requiredLevel);
        return actual >= required;
    }

    private int levelToInt(String level) {
        return switch (level.toUpperCase()) {
            case "READ" -> 1;
            case "WRITE" -> 2;
            case "ADMIN" -> 3;
            default -> 0;
        };
    }
}
