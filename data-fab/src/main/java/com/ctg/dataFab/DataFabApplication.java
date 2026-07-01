package com.ctg.dataFab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 数据集建设子系统启动类
 *
 * @author Developer
 * @since 2026-07-01
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class DataFabApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataFabApplication.class, args);
    }
}
