package com.ctg.kbFab.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 知识状态
 *
 * @author Developer
 * @since 2026-07-01
 */
@Getter
public enum KnowledgeStatus {
    DRAFT("draft", "草稿"),
    PUBLISHED("published", "已发布"),
    ARCHIVED("archived", "已归档"),
    DEPRECATED("deprecated", "已废弃");

    @EnumValue
    private final String code;
    private final String description;

    KnowledgeStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
