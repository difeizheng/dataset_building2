package com.ctg.dataFab.etl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.common.exception.BusinessException;
import com.ctg.dataFab.etl.entity.EtlRule;
import com.ctg.dataFab.etl.mapper.EtlRuleMapper;
import com.ctg.dataFab.etl.service.EtlRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 清洗规则服务实现
 *
 * @author Developer
 * @since 2026-07-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EtlRuleServiceImpl implements EtlRuleService {

    private final EtlRuleMapper etlRuleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRule(EtlRule rule) {
        log.info("创建清洗规则: {}", rule.getName());
        rule.setEnabled(1); // 默认启用
        etlRuleMapper.insert(rule);
        log.info("清洗规则创建成功, ID: {}", rule.getId());
        return rule.getId();
    }

    @Override
    public EtlRule getRuleById(Long id) {
        EtlRule rule = etlRuleMapper.selectById(id);
        if (rule == null) {
            throw new BusinessException("清洗规则不存在");
        }
        return rule;
    }

    @Override
    public Page<EtlRule> listRules(Integer modality, String ruleType, PageRequest pageRequest) {
        LambdaQueryWrapper<EtlRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(modality != null, EtlRule::getModality, modality)
               .eq(ruleType != null, EtlRule::getRuleType, ruleType)
               .orderByDesc(EtlRule::getPriority)
               .orderByDesc(EtlRule::getCreateTime);

        Page<EtlRule> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        return etlRuleMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRule(EtlRule rule) {
        etlRuleMapper.updateById(rule);
        log.info("清洗规则更新成功, ID: {}", rule.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRule(Long id) {
        getRuleById(id);
        etlRuleMapper.deleteById(id);
        log.info("清洗规则删除成功, ID: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleRuleEnabled(Long id, Integer enabled) {
        EtlRule rule = getRuleById(id);
        rule.setEnabled(enabled);
        etlRuleMapper.updateById(rule);
        log.info("清洗规则启用状态更新成功, ID: {}, 启用: {}", id, enabled);
    }
}
