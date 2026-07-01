package com.ctg.kbFab.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 知识类型枚举
 *
 * @author Developer
 * @since 2026-07-01
 */
@Getter
public enum KnowledgeType {
    ENTITY("entity", "实体"),
    RELATION("relation", "关系"),
    ATTRIBUTE("attribute", "属性"),
    EVENT("event", "事件"),
    CONCEPT("concept", "概念");

    @EnumValue
    private final String code;
    private final String description;

    KnowledgeType(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
