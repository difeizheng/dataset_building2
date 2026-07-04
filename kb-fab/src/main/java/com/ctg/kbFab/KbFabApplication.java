package com.ctg.kbFab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 知识库建设子系统启动类
 *
 * @author Developer
 * @since 2026-07-01
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class KbFabApplication {

    public static void main(String[] args) {
        SpringApplication.run(KbFabApplication.class, args);
    }
}
