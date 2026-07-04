package com.ctg.kbFab.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 抽取任务状态
 *
 * @author Developer
 * @since 2026-07-01
 */
@Getter
public enum ExtractionStatus {
    PENDING("pending", "待处理"),
    PROCESSING("processing", "处理中"),
    COMPLETED("completed", "已完成"),
    FAILED("failed", "失败");

    @EnumValue
    private final String code;
    private final String description;

    ExtractionStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
