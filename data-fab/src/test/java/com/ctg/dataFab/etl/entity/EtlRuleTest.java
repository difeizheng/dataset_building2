package com.ctg.dataFab.etl.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EtlRule Entity 单元测试
 *
 * @author Security Officer
 * @since 2026-07-05
 */
class EtlRuleTest {

    @Test
    void testGettersAndSetters() {
        EtlRule rule = new EtlRule();
        rule.setId(1L);
        rule.setName("数据清洗规则");
        rule.setDescription("用于去除重复数据");
        rule.setModality(1);
        rule.setRuleType("去重");
        rule.setRuleConfig("{\"dedupKey\":\"name\"}");
        rule.setPriority(1);
        rule.setEnabled(1);
        rule.setCreateBy("admin");
        rule.setCreateTime(LocalDateTime.now());
        rule.setUpdateBy("admin");
        rule.setUpdateTime(LocalDateTime.now());
        rule.setDeleted(0);

        assertEquals(1L, rule.getId());
        assertEquals("数据清洗规则", rule.getName());
        assertEquals("用于去除重复数据", rule.getDescription());
        assertEquals(1, rule.getModality());
        assertEquals("去重", rule.getRuleType());
        assertEquals("{\"dedupKey\":\"name\"}", rule.getRuleConfig());
        assertEquals(1, rule.getPriority());
        assertEquals(1, rule.getEnabled());
        assertEquals("admin", rule.getCreateBy());
        assertNotNull(rule.getCreateTime());
        assertEquals("admin", rule.getUpdateBy());
        assertNotNull(rule.getUpdateTime());
        assertEquals(0, rule.getDeleted());
    }

    @Test
    void testToString() {
        EtlRule rule = new EtlRule();
        rule.setId(1L);
        rule.setName("规则1");

        String str = rule.toString();
        assertNotNull(str);
        assertTrue(str.contains("EtlRule"));
    }

    @Test
    void testEqualsAndHashCode() {
        EtlRule r1 = new EtlRule();
        r1.setId(1L);
        r1.setName("规则1");

        EtlRule r2 = new EtlRule();
        r2.setId(1L);
        r2.setName("规则1");

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }
}