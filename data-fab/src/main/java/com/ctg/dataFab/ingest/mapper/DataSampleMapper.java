package com.ctg.dataFab.ingest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ctg.dataFab.ingest.entity.DataSample;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据样本Mapper
 *
 * @author Developer
 * @since 2026-07-01
 */
@Mapper
public interface DataSampleMapper extends BaseMapper<DataSample> {
}
