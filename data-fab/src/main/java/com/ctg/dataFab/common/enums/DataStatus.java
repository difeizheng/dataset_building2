package com.ctg.dataFab.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据样本状态枚举
 * 状态机：new -> raw -> clean -> labeled -> qa-pass -> published
 *
 * @author Developer
 * @since 2026-07-01
 */
@Getter
@AllArgsConstructor
public enum DataStatus {

    NEW(0, "新建"),
    RAW(1, "原始数据"),
    CLEAN(2, "已清洗"),
    LABELED(3, "已标注"),
    QA_PASS(4, "质量通过"),
    QA_FAIL(5, "质量不通过"),
    PUBLISHED(6, "已发布"),
    ARCHIVED(7, "已归档");

    private final int code;
    private final String name;

    /**
     * 根据code获取枚举
     */
    public static DataStatus fromCode(int code) {
        for (DataStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status code: " + code);
    }
}
