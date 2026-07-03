package com.ctg.integration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import com.ctg.integration.security.JwtAuthenticationFilter;
import com.ctg.integration.security.JwtTokenProvider;
import com.ctg.integration.security.RateLimitingFilter;

import lombok.RequiredArgsConstructor;

/**
 * Spring Security配置
 * 配置OAuth2.0 + JWT认证，支持国密算法
 * 安全加固：
 * - C-1: 所有API端点通过@PreAuthorize或SecurityFilterChain保护
 * - H-2: 集成RateLimitingFilter进行API限流
 * - H-4: 启用CSRF防护（CookieCsrfTokenRepository）
 * - 生产环境关闭Swagger
 *
 * @author CTG
 * @since 2026-07-01
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final RateLimitingFilter rateLimitingFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
        csrfHandler.setCsrfRequestAttributeName(null);

        http
            // H-4: 启用CSRF防护（前后端token校验）
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(csrfHandler)
                // 对认证相关接口和webhook豁免CSRF（JWT已提供认证保障）
                .ignoringRequestMatchers(
                    "/auth/login",
                    "/auth/refresh",
                    "/auth/validate",
                    "/actuator/**"
                ))
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 公开接口（无需认证）
                .requestMatchers("/auth/login").permitAll()
                .requestMatchers("/auth/refresh").permitAll()
                .requestMatchers("/auth/validate").permitAll()
                .requestMatchers("/health/**").permitAll()
                .requestMatchers("/monitor/health").permitAll()
                // 生产环境关闭Swagger（通过profile控制）
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                // 其他接口需要认证（具体权限由@PreAuthorize控制）
                .anyRequest().authenticated())
            // H-2: 添加限流过滤器
            .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
            // JWT认证过滤器
            .addFilterAfter(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
