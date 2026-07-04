package com.ctg.kbFab.extraction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ctg.kbFab.extraction.entity.ExtractionTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 抽取任务Mapper
 *
 * @author Developer
 * @since 2026-07-01
 */
@Mapper
public interface ExtractionTaskMapper extends BaseMapper<ExtractionTask> {
}
