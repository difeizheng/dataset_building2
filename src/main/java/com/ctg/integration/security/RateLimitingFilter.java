package com.ctg.integration.security;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

/**
 * API限流过滤器
 * 基于Resilience4j RateLimiter实现
 * 防止API滥用和DDoS攻击
 *
 * @author CTG
 * @since 2026-07-03
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimiterRegistry rateLimiterRegistry;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String clientIp = getClientIp(request);
        String endpoint = request.getRequestURI();

        // 获取或创建该客户端的限流器
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(
            "client:" + clientIp,
            RateLimiterConfig.custom()
                .limitForPeriod(100)  // 每个周期允许100次请求
                .limitRefreshPeriod(Duration.ofMinutes(1))  // 每分钟刷新
                .timeoutDuration(Duration.ofMillis(500))  // 等待许可的最大时间
                .build()
        );

        // 尝试获取许可
        if (rateLimiter.acquirePermission()) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("API限流触发: clientIp={}, endpoint={}", clientIp, endpoint);
            response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"请求过于频繁，请稍后再试\"}");
        }
    }

    /**
     * 获取客户端真实IP
     * 支持代理服务器场景
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
