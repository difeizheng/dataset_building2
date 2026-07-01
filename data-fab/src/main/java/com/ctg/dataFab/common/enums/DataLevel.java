package com.ctg.dataFab.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据分级枚举 (M1 数据分级 L1~L4)
 * L1: 公开 - 可对外发布
 * L2: 内部 - 集团内部一般业务
 * L3: 敏感 - 内部重要数据
 * L4: 核心 - 集团核心竞争优势、国家安全
 *
 * @author Developer
 * @since 2026-07-01
 */
@Getter
@AllArgsConstructor
public enum DataLevel {

    L1_PUBLIC(1, "公开", "可对外发布的数据"),
    L2_INTERNAL(2, "内部", "集团内部一般业务数据"),
    L3_SENSITIVE(3, "敏感", "内部重要数据"),
    L4_CORE(4, "核心", "集团核心竞争优势、国家安全相关数据");

    private final int code;
    private final String name;
    private final String description;

    /**
     * 根据code获取枚举
     */
    public static DataLevel fromCode(int code) {
        for (DataLevel level : values()) {
            if (level.code == code) {
                return level;
            }
        }
        throw new IllegalArgumentException("Invalid data level code: " + code);
    }
}
