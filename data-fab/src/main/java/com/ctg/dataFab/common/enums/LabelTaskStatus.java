package com.ctg.dataFab.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 标注任务状态枚举
 *
 * @author Developer
 * @since 2026-07-01
 */
@Getter
@AllArgsConstructor
public enum LabelTaskStatus {

    DRAFT(0, "草稿"),
    PENDING(1, "待分配"),
    IN_PROGRESS(2, "标注中"),
    ARBITRATING(3, "仲裁中"),
    COMPLETED(4, "已完成"),
    REJECTED(5, "已驳回"),
    CANCELLED(6, "已取消");

    private final int code;
    private final String name;

    /**
     * 根据code获取枚举
     */
    public static LabelTaskStatus fromCode(int code) {
        for (LabelTaskStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid label task status code: " + code);
    }
}
