package com.ctg.integration.service;

import java.util.List;
import java.util.Map;

import com.ctg.integration.dto.monitor.*;

/**
 * 运维监控服务接口
 *
 * @author CTG
 * @since 2026-07-01
 */
public interface MonitorService {

    /**
     * 获取系统健康状态
     */
    HealthStatusVO getHealthStatus();

    /**
     * 获取性能指标
     */
    List<MetricVO> getMetrics(String metricType);

    /**
     * 获取系统资源使用情况
     */
    SystemResourceVO getSystemResources();

    /**
     * 获取告警列表
     */
    List<AlertVO> getAlerts(AlertQuery query);

    /**
     * 确认告警
     */
    void acknowledgeAlert(Long alertId, String operator);

    /**
     * 获取监控统计数据
     */
    MonitorStatisticsVO getStatistics(String startTime, String endTime);
}
