package com.ctg.kbFab.common.config;

import com.ctg.kbFab.common.security.JwtAuthenticationFilter;
import com.ctg.kbFab.common.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
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

/**
 * Spring Security 配置
 * 集成 JWT 认证和权限控制
 *
 * @author Developer
 * @since 2026-07-03
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/api/v1/knowledge/**").hasAnyRole("ADMIN", "OPERATOR")
                .requestMatchers("/api/v1/extraction/**").hasAnyRole("ADMIN", "OPERATOR")
                .requestMatchers("/api/v1/ontology/**").hasAnyRole("ADMIN", "OPERATOR")
                .requestMatchers("/api/v1/retrieval/**").authenticated()
                .requestMatchers("/api/v1/rag/**").authenticated()
                .requestMatchers("/api/v1/quality/**").hasAnyRole("ADMIN", "AUDITOR")
                .requestMatchers("/api/v1/access/**").hasAnyRole("ADMIN", "AUDITOR")
                .anyRequest().authenticated())
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
