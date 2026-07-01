package com.ctg.dataFab.ingest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ctg.dataFab.ingest.entity.Dataset;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据集Mapper
 *
 * @author Developer
 * @since 2026-07-01
 */
@Mapper
public interface DatasetMapper extends BaseMapper<Dataset> {
}
