package com.ctg.dataFab.etl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ctg.dataFab.etl.entity.EtlRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 清洗规则Mapper
 *
 * @author Developer
 * @since 2026-07-01
 */
@Mapper
public interface EtlRuleMapper extends BaseMapper<EtlRule> {
}
