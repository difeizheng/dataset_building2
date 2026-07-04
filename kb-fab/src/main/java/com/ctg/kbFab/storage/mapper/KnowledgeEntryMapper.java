package com.ctg.kbFab.storage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ctg.kbFab.storage.entity.KnowledgeEntry;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识条目Mapper
 *
 * @author Developer
 * @since 2026-07-01
 */
@Mapper
public interface KnowledgeEntryMapper extends BaseMapper<KnowledgeEntry> {
}
