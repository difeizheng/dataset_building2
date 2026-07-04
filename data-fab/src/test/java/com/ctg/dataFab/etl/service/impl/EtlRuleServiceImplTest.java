package com.ctg.dataFab.etl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.common.exception.BusinessException;
import com.ctg.dataFab.etl.entity.EtlRule;
import com.ctg.dataFab.etl.mapper.EtlRuleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 清洗规则服务单元测试
 *
 * @author Developer
 * @since 2026-07-05
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("清洗规则服务测试")
class EtlRuleServiceImplTest {

    @Mock
    private EtlRuleMapper etlRuleMapper;

    @InjectMocks
    private EtlRuleServiceImpl etlRuleService;

    private EtlRule etlRule;

    @BeforeEach
    void setUp() {
        etlRule = new EtlRule();
        etlRule.setId(1L);
        etlRule.setName("测试规则");
        etlRule.setModality(1);
        etlRule.setRuleType("cleaning");
        etlRule.setRuleConfig("{}");
        etlRule.setPriority(10);
        etlRule.setEnabled(1);
    }

    @Test
    @DisplayName("创建规则 - 默认启用")
    void testCreateRule_DefaultEnabled() {
        etlRule.setEnabled(null); // 未设置启用状态

        when(etlRuleMapper.insert(any(EtlRule.class))).thenAnswer(invocation -> {
            EtlRule rule = invocation.getArgument(0);
            rule.setId(1L);
            return 1;
        });

        Long id = etlRuleService.createRule(etlRule);

        assertNotNull(id);
        assertEquals(1, etlRule.getEnabled()); // 应该被设置为默认启用
        verify(etlRuleMapper).insert(any(EtlRule.class));
    }

    @Test
    @DisplayName("获取规则 - 存在")
    void testGetRuleById_Exists() {
        when(etlRuleMapper.selectById(1L)).thenReturn(etlRule);

        EtlRule result = etlRuleService.getRuleById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("测试规则", result.getName());
    }

    @Test
    @DisplayName("获取规则 - 不存在")
    void testGetRuleById_NotExists() {
        when(etlRuleMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> {
            etlRuleService.getRuleById(999L);
        });
    }

    @Test
    @DisplayName("分页查询规则 - 无过滤条件")
    void testListRules_NoFilter() {
        Page<EtlRule> page = new Page<>(1, 20);
        page.setRecords(Collections.singletonList(etlRule));

        when(etlRuleMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(1);
        pageRequest.setPageSize(20);

        Page<EtlRule> result = etlRuleService.listRules(null, null, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("分页查询规则 - 按模态过滤")
    void testListRules_FilterByModality() {
        Page<EtlRule> page = new Page<>(1, 20);
        page.setRecords(Collections.singletonList(etlRule));

        when(etlRuleMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(1);
        pageRequest.setPageSize(20);

        Page<EtlRule> result = etlRuleService.listRules(1, null, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("分页查询规则 - 按类型过滤")
    void testListRules_FilterByType() {
        Page<EtlRule> page = new Page<>(1, 20);
        page.setRecords(Collections.singletonList(etlRule));

        when(etlRuleMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(1);
        pageRequest.setPageSize(20);

        Page<EtlRule> result = etlRuleService.listRules(null, "cleaning", pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("更新规则")
    void testUpdateRule() {
        when(etlRuleMapper.updateById(any(EtlRule.class))).thenReturn(1);

        etlRuleService.updateRule(etlRule);

        verify(etlRuleMapper).updateById(any(EtlRule.class));
    }

    @Test
    @DisplayName("删除规则 - 存在")
    void testDeleteRule_Exists() {
        when(etlRuleMapper.selectById(1L)).thenReturn(etlRule);
        when(etlRuleMapper.deleteById(1L)).thenReturn(1);

        etlRuleService.deleteRule(1L);

        verify(etlRuleMapper).selectById(1L);
        verify(etlRuleMapper).deleteById(1L);
    }

    @Test
    @DisplayName("删除规则 - 不存在")
    void testDeleteRule_NotExists() {
        when(etlRuleMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> {
            etlRuleService.deleteRule(999L);
        });
    }

    @Test
    @DisplayName("切换规则启用状态 - 启用")
    void testToggleRuleEnabled_Enable() {
        when(etlRuleMapper.selectById(1L)).thenReturn(etlRule);
        when(etlRuleMapper.updateById(any(EtlRule.class))).thenReturn(1);

        etlRuleService.toggleRuleEnabled(1L, 1);

        verify(etlRuleMapper).selectById(1L);
        verify(etlRuleMapper).updateById(any(EtlRule.class));
    }

    @Test
    @DisplayName("切换规则启用状态 - 禁用")
    void testToggleRuleEnabled_Disable() {
        when(etlRuleMapper.selectById(1L)).thenReturn(etlRule);
        when(etlRuleMapper.updateById(any(EtlRule.class))).thenReturn(1);

        etlRuleService.toggleRuleEnabled(1L, 0);

        verify(etlRuleMapper).selectById(1L);
        verify(etlRuleMapper).updateById(any(EtlRule.class));
    }

    @Test
    @DisplayName("切换规则启用状态 - 规则不存在")
    void testToggleRuleEnabled_RuleNotExists() {
        when(etlRuleMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> {
            etlRuleService.toggleRuleEnabled(999L, 1);
        });
    }
}
