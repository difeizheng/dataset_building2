package com.ctg.dataFab.common.config;

import com.ctg.dataFab.common.security.JwtAuthenticationFilter;
import com.ctg.dataFab.common.security.RateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 配置类
 * 实现认证、授权、CSRF、CORS 安全配置
 *
 * 安全策略：
 * - /api/** 全部需要 JWT 认证
 * - L1 公开数据 API 可公开查询（GET /api/v1/data/samples 等）
 * - L2/L3/L4 API 必须登录 + 角色权限
 * - L4 端点需要额外 MFA
 *
 * @author Developer
 * @since 2026-07-01
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;

    /**
     * 安全过滤器链
     *
     * 授权策略:
     * - /api/v1/auth/**         — 公开 (登录/注册)
     * - /api/v1/data/samples    — L1 公开查询 (GET); L2+ 写操作需 ADMIN/OPERATOR
     * - /api/v1/data/datasets   — L1 公开查询 (GET); L2+ 需相应角色
     * - /api/v1/label/**        — 需 ADMIN/OPERATOR
     * - /api/v1/qa/**           — 需 ADMIN/OPERATOR/AUDITOR
     * - /api/v1/etl/**          — 需 ADMIN/OPERATOR
     * - /api/v1/delivery/**     — 需 ADMIN/OPERATOR; L4下载需 MFA
     * - /api/v1/admin/**        — 仅 ADMIN (更严格限流: 5 req/s)
     * - /api/v1/audit/**        — 仅 ADMIN/AUDITOR
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF (使用 JWT 时不需要)
                .csrf(AbstractHttpConfigurer::disable)

                // 配置 CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 配置会话管理 (无状态)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 配置授权规则
                .authorizeHttpRequests(auth -> auth
                        // 公开接口 - 无需认证
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/health", "/info").permitAll()

                        // 登录注册接口 - 公开
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // L1 公开数据查询 — GET /api/v1/data/samples 和 /datasets 可匿名
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/data/samples").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/data/samples/*").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/data/datasets").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/data/datasets/*").permitAll()

                        // 数据集管理接口 - L1 读公开，写操作需 ADMIN/OPERATOR
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/data/**").hasAnyRole("ADMIN", "OPERATOR")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/v1/data/**").hasAnyRole("ADMIN", "OPERATOR")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/v1/data/**").hasRole("ADMIN")

                        // 标注管理接口 - 需要 ADMIN 或 OPERATOR
                        .requestMatchers("/api/v1/label/**").hasAnyRole("ADMIN", "OPERATOR")

                        // 质量评估接口 - 需要 ADMIN, OPERATOR 或 AUDITOR
                        .requestMatchers("/api/v1/qa/**").hasAnyRole("ADMIN", "OPERATOR", "AUDITOR")

                        // ETL 接口 - 需要 ADMIN 或 OPERATOR
                        .requestMatchers("/api/v1/etl/**").hasAnyRole("ADMIN", "OPERATOR")

                        // 交付接口 - ADMIN 可发布/下架; OPERATOR 可操作; 下载需 MFA (在 Controller 层验证)
                        .requestMatchers("/api/v1/delivery/**").hasAnyRole("ADMIN", "OPERATOR")

                        // 管理员专属接口 - 仅 ADMIN
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // 审计日志接口 - 需要 ADMIN 或 AUDITOR 角色
                        .requestMatchers("/api/v1/audit/**").hasAnyRole("ADMIN", "AUDITOR")

                        // 其他所有请求需要认证
                        .anyRequest().authenticated()
                )

                // 添加 JWT 过滤器（认证）
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 添加限流过滤器（在认证之后，便于按用户限流）
                .addFilterAfter(rateLimitFilter, JwtAuthenticationFilter.class)

                // 配置异常处理
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(401);
                            response.getWriter().write("{\"code\":401,\"message\":\"未授权，请先登录\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(403);
                            response.getWriter().write("{\"code\":403,\"message\":\"访问被拒绝\"}");
                        })
                );

        return http.build();
    }

    /**
     * CORS 配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 允许的源
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:8080",
                "https://your-domain.com"
        ));

        // 允许的方法
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // 允许的头
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers",
                "X-MFA-Token"
        ));

        // 允许凭证
        configuration.setAllowCredentials(true);

        // 预检请求缓存时间
        configuration.setMaxAge(3600L);

        // 暴露的头
        configuration.setExposedHeaders(Arrays.asList(
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
