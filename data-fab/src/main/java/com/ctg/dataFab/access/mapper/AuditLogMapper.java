package com.ctg.dataFab.access.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ctg.dataFab.access.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审计日志Mapper
 *
 * @author Developer
 * @since 2026-07-01
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}
