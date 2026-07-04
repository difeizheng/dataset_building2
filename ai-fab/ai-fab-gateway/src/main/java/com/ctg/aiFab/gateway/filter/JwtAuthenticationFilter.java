package com.ctg.aiFab.gateway.filter;

import com.ctg.aiFab.gateway.common.dto.ApiResponse;
import com.ctg.aiFab.gateway.service.JwtService;
import com.ctg.aiFab.gateway.service.RateLimitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 *
 * @author Developer
 * @since 2026-07-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    @Value("${rate-limit.default:100}")
    private int defaultRateLimit;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    jakarta.servlet.FilterChain filterChain)
            throws jakarta.servlet.ServletException, IOException {

        String path = request.getRequestURI();

        // 白名单路径
        if (path.startsWith("/auth/") || path.startsWith("/swagger-ui/") ||
            path.startsWith("/v3/api-docs") || path.startsWith("/actuator/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 限流检查
        String clientIp = getClientIp(request);
        String rateLimitKey = clientIp + ":" + path;
        if (rateLimitService.isRateLimited(rateLimitKey, defaultRateLimit, 1000)) {
            log.warn("请求被限流: {}", rateLimitKey);
            writeErrorResponse(response, 429, "请求过于频繁，请稍后重试");
            return;
        }

        // JWT验证
        String token = extractToken(request);
        if (token == null || !jwtService.isTokenValid(token)) {
            writeErrorResponse(response, 401, "未授权访问");
            return;
        }

        try {
            Long userId = jwtService.getUserIdFromToken(token);
            String username = jwtService.getUsernameFromToken(token);
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("JWT解析失败: {}", e.getMessage());
            writeErrorResponse(response, 401, "认证失败");
        }
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip.split(",")[0].trim();
    }

    private void writeErrorResponse(HttpServletResponse response, int code, String message)
            throws IOException {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        ApiResponse<Void> apiResponse = ApiResponse.error(code, message);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
