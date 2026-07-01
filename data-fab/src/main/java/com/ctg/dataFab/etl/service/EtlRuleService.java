package com.ctg.dataFab.etl.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.etl.entity.EtlRule;

/**
 * 清洗规则服务接口
 *
 * @author Developer
 * @since 2026-07-01
 */
public interface EtlRuleService {

    /**
     * 创建清洗规则
     */
    Long createRule(EtlRule rule);

    /**
     * 根据ID获取清洗规则
     */
    EtlRule getRuleById(Long id);

    /**
     * 分页查询清洗规则
     */
    Page<EtlRule> listRules(Integer modality, String ruleType, PageRequest pageRequest);

    /**
     * 更新清洗规则
     */
    void updateRule(EtlRule rule);

    /**
     * 删除清洗规则
     */
    void deleteRule(Long id);

    /**
     * 启用/禁用清洗规则
     */
    void toggleRuleEnabled(Long id, Integer enabled);
}
