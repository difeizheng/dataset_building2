package com.ctg.dataFab.etl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctg.dataFab.common.dto.PageRequest;
import com.ctg.dataFab.common.exception.BusinessException;
import com.ctg.dataFab.etl.entity.EtlRule;
import com.ctg.dataFab.etl.entity.EtlTask;
import com.ctg.dataFab.etl.mapper.EtlRuleMapper;
import com.ctg.dataFab.etl.mapper.EtlTaskMapper;
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
 * ETL服务单元测试
 *
 * @author Developer
 * @since 2026-07-01
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ETL服务测试")
class EtlServiceTest {

    @Mock
    private EtlRuleMapper etlRuleMapper;

    @Mock
    private EtlTaskMapper etlTaskMapper;

    @InjectMocks
    private EtlRuleServiceImpl etlRuleService;

    @InjectMocks
    private EtlTaskServiceImpl etlTaskService;

    private EtlRule etlRule;
    private EtlTask etlTask;

    @BeforeEach
    void setUp() {
        etlRule = new EtlRule();
        etlRule.setId(1L);
        etlRule.setName("测试规则");
        etlRule.setModality(1);
        etlRule.setRuleType("cleaning");
        etlRule.setRuleConfig("{}");

        etlTask = new EtlTask();
        etlTask.setId(1L);
        etlTask.setTaskName("测试任务");
        etlTask.setDatasetId(1L);
        etlTask.setStatus(0);
    }

    @Test
    @DisplayName("创建ETL规则")
    void testCreateRule() {
        when(etlRuleMapper.insert(any(EtlRule.class))).thenAnswer(invocation -> {
            EtlRule rule = invocation.getArgument(0);
            rule.setId(1L);
            return 1;
        });

        Long id = etlRuleService.createRule(etlRule);

        assertNotNull(id);
        verify(etlRuleMapper).insert(any(EtlRule.class));
    }

    @Test
    @DisplayName("获取ETL规则 - 存在")
    void testGetRuleById_Exists() {
        when(etlRuleMapper.selectById(1L)).thenReturn(etlRule);

        EtlRule result = etlRuleService.getRuleById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("获取ETL规则 - 不存在")
    void testGetRuleById_NotExists() {
        when(etlRuleMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> {
            etlRuleService.getRuleById(999L);
        });
    }

    @Test
    @DisplayName("分页查询ETL规则")
    void testListRules() {
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
    @DisplayName("更新ETL规则")
    void testUpdateRule() {
        when(etlRuleMapper.updateById(any(EtlRule.class))).thenReturn(1);

        etlRuleService.updateRule(etlRule);

        verify(etlRuleMapper).updateById(any(EtlRule.class));
    }

    @Test
    @DisplayName("删除ETL规则")
    void testDeleteRule() {
        when(etlRuleMapper.selectById(1L)).thenReturn(etlRule);
        when(etlRuleMapper.deleteById(1L)).thenReturn(1);

        etlRuleService.deleteRule(1L);

        verify(etlRuleMapper).deleteById(1L);
    }

    @Test
    @DisplayName("创建ETL任务")
    void testCreateTask() {
        when(etlTaskMapper.insert(any(EtlTask.class))).thenAnswer(invocation -> {
            EtlTask task = invocation.getArgument(0);
            task.setId(1L);
            return 1;
        });

        Long id = etlTaskService.createTask(1L, "测试任务");

        assertNotNull(id);
        verify(etlTaskMapper).insert(any(EtlTask.class));
    }

    @Test
    @DisplayName("获取ETL任务 - 存在")
    void testGetTaskById_Exists() {
        when(etlTaskMapper.selectById(1L)).thenReturn(etlTask);

        EtlTask result = etlTaskService.getTaskById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("获取ETL任务 - 不存在")
    void testGetTaskById_NotExists() {
        when(etlTaskMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> {
            etlTaskService.getTaskById(999L);
        });
    }

    @Test
    @DisplayName("分页查询ETL任务")
    void testListTasks() {
        Page<EtlTask> page = new Page<>(1, 20);
        page.setRecords(Collections.singletonList(etlTask));

        when(etlTaskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(1);
        pageRequest.setPageSize(20);

        Page<EtlTask> result = etlTaskService.listTasks(null, null, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("执行ETL任务")
    void testExecuteTask() {
        when(etlTaskMapper.selectById(1L)).thenReturn(etlTask);
        when(etlTaskMapper.updateById(any(EtlTask.class))).thenReturn(1);

        etlTaskService.executeTask(1L);

        verify(etlTaskMapper, atLeastOnce()).updateById(any(EtlTask.class));
    }
}
