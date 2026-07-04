package com.ctg.dataFab.ingest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ctg.dataFab.ingest.entity.DataSample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

/**
 * 数据样本Mapper
 *
 * @author Developer
 * @since 2026-07-01
 */
@Mapper
public interface DataSampleMapper extends BaseMapper<DataSample> {

    /**
     * M-5: 使用SQL SUM()聚合计算总大小，避免全量加载
     */
    @Select("SELECT COALESCE(SUM(file_size), 0) FROM t_data_sample WHERE dataset_id = #{datasetId} AND deleted = 0")
    Long sumFileSizeByDatasetId(@Param("datasetId") Long datasetId);
}
