package com.ctg.aiFab.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 限流服务 - 基于Redis滑动窗口
 *
 * @author Developer
 * @since 2026-07-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;

    /**
     * 检查是否超过限流阈值
     *
     * @param key      限流key（如 userId:apiPath）
     * @param limit    每秒限制次数
     * @param windowMs 窗口大小（毫秒）
     * @return true=超过限制
     */
    public boolean isRateLimited(String key, int limit, long windowMs) {
        try {
            String redisKey = "rate_limit:" + key;
            long now = System.currentTimeMillis();
            long windowStart = now - windowMs;

            // 移除窗口外的记录
            redisTemplate.opsForZSet().removeRangeByScore(redisKey, 0, windowStart);

            // 统计窗口内请求数
            Long count = redisTemplate.opsForZSet().zCard(redisKey);
            if (count != null && count >= limit) {
                return true;
            }

            // 添加当前请求
            redisTemplate.opsForZSet().add(redisKey, String.valueOf(now), now);
            redisTemplate.expire(redisKey, windowMs + 1000, TimeUnit.MILLISECONDS);

            return false;
        } catch (Exception e) {
            log.error("限流检查异常: {}", e.getMessage());
            return false; // Redis故障时降级放行
        }
    }

    /**
     * 获取当前请求速率
     */
    public long getCurrentRate(String key, long windowMs) {
        try {
            String redisKey = "rate_limit:" + key;
            long now = System.currentTimeMillis();
            long windowStart = now - windowMs;
            redisTemplate.opsForZSet().removeRangeByScore(redisKey, 0, windowStart);
            Long count = redisTemplate.opsForZSet().zCard(redisKey);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
