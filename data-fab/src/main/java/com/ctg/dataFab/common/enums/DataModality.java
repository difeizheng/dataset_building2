package com.ctg.dataFab.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据模态枚举
 * 支持文本/图像/音频/视频四模态
 *
 * @author Developer
 * @since 2026-07-01
 */
@Getter
@AllArgsConstructor
public enum DataModality {

    TEXT(1, "文本"),
    IMAGE(2, "图像"),
    AUDIO(3, "音频"),
    VIDEO(4, "视频");

    private final int code;
    private final String name;

    /**
     * 根据code获取枚举
     */
    public static DataModality fromCode(int code) {
        for (DataModality modality : values()) {
            if (modality.code == code) {
                return modality;
            }
        }
        throw new IllegalArgumentException("Invalid modality code: " + code);
    }

    /**
     * 根据名称获取枚举
     */
    public static DataModality fromName(String name) {
        for (DataModality modality : values()) {
            if (modality.name.equals(name)) {
                return modality;
            }
        }
        throw new IllegalArgumentException("Invalid modality name: " + name);
    }
}
