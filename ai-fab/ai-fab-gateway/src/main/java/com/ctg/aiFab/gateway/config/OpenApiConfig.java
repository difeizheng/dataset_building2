package com.ctg.aiFab.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3.0配置
 *
 * @author Developer
 * @since 2026-07-01
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI能力封装子系统 API")
                        .version("1.0.0")
                        .description("三峡集团人工智能能力封装子系统接口文档")
                        .contact(new Contact()
                                .name("Developer")
                                .email("developer@ctg.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("本地开发环境"),
                        new Server().url("https://api.ctg.com").description("生产环境")
                ));
    }
}
