package com.ctg.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 集成与管控子系统主应用
 *
 * @author CTG
 * @since 2026-07-01
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class IntegrationControlApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntegrationControlApplication.class, args);
    }
}
