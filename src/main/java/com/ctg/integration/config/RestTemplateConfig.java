package com.ctg.integration.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate配置
 * 配置超时参数以避免长时间阻塞
 *
 * @author CTG
 * @since 2026-07-01
 */
@Configuration
public class RestTemplateConfig {

    @Value("${integration.rest-template.connect-timeout:5000}")
    private int connectTimeout;

    @Value("${integration.rest-template.read-timeout:30000}")
    private int readTimeout;

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        return new RestTemplate(factory);
    }
}
