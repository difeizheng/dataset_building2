package com.ctg.integration.security;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 数据脱敏引擎
 * 支持多种脱敏策略，满足数据安全分级管控要求（L1-L4）
 *
 * @author CTG
 * @since 2026-07-01
 */
@Slf4j
@Component
public class DataMaskingEngine {

    private static final Map<String, MaskingStrategy> STRATEGIES = new HashMap<>();

    static {
        // 手机号脱敏：138****1234
        STRATEGIES.put("phone", (data) -> {
            if (data == null || data.length() < 7) return data;
            return data.substring(0, 3) + "****" + data.substring(data.length() - 4);
        });

        // 身份证脱敏：110***********1234
        STRATEGIES.put("idCard", (data) -> {
            if (data == null || data.length() < 8) return data;
            return data.substring(0, 3) + "***********" + data.substring(data.length() - 4);
        });

        // 邮箱脱敏：a***b@example.com
        STRATEGIES.put("email", (data) -> {
            if (data == null || !data.contains("@")) return data;
            int atIndex = data.indexOf("@");
            if (atIndex <= 2) return data;
            return data.charAt(0) + "***" + data.charAt(atIndex - 1) + data.substring(atIndex);
        });

        // 姓名脱敏：张*
        STRATEGIES.put("name", (data) -> {
            if (data == null || data.length() < 2) return data;
            return data.charAt(0) + "*".repeat(data.length() - 1);
        });

        // 银行卡脱敏：6222 **** **** 1234
        STRATEGIES.put("bankCard", (data) -> {
            if (data == null || data.length() < 8) return data;
            return data.substring(0, 4) + " **** **** " + data.substring(data.length() - 4);
        });

        // 地址脱敏：保留省市，隐藏详细地址
        STRATEGIES.put("address", (data) -> {
            if (data == null || data.length() < 6) return data;
            return data.substring(0, 6) + "****";
        });

        // 完全隐藏
        STRATEGIES.put("full", (data) -> "******");

        // 数字脱敏：保留位数，用*替换
        STRATEGIES.put("number", (data) -> {
            if (data == null) return data;
            return "*".repeat(data.length());
        });
    }

    /**
     * 根据脱敏类型执行脱敏
     */
    public String mask(String data, String maskType) {
        if (data == null || data.isEmpty()) {
            return data;
        }
        MaskingStrategy strategy = STRATEGIES.get(maskType);
        if (strategy == null) {
            log.warn("未知的脱敏类型: {}，使用完全隐藏策略", maskType);
            strategy = STRATEGIES.get("full");
        }
        return strategy.mask(data);
    }

    /**
     * 根据数据级别自动选择脱敏策略
     * L1: 公开数据 - 不脱敏
     * L2: 内部数据 - 轻度脱敏
     * L3: 敏感数据 - 中度脱敏
     * L4: 机密数据 - 重度脱敏
     */
    public String maskByLevel(String data, String dataType, String securityLevel) {
        if (data == null || data.isEmpty()) {
            return data;
        }

        return switch (securityLevel) {
            case "L1" -> data; // 公开数据不脱敏
            case "L2" -> maskLightly(data, dataType);
            case "L3" -> mask(data, dataType);
            case "L4" -> STRATEGIES.get("full").mask(data);
            default -> mask(data, dataType);
        };
    }

    /**
     * 轻度脱敏（L2级别）
     */
    private String maskLightly(String data, String dataType) {
        if (data == null) return data;

        return switch (dataType) {
            case "phone" -> {
                if (data.length() >= 7) yield data.substring(0, 3) + "**" + data.substring(data.length() - 4);
                yield data;
            }
            case "idCard" -> {
                if (data.length() >= 8) yield data.substring(0, 6) + "******" + data.substring(data.length() - 4);
                yield data;
            }
            case "email" -> mask(data, "email");
            case "name" -> data.length() >= 2 ? data.charAt(0) + "*" : data;
            default -> mask(data, dataType);
        };
    }

    /**
     * 批量脱敏
     */
    public Map<String, String> maskMap(Map<String, String> data, Map<String, String> fieldMaskTypes) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String maskType = fieldMaskTypes.get(entry.getKey());
            if (maskType != null) {
                result.put(entry.getKey(), mask(entry.getValue(), maskType));
            } else {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * 脱敏策略函数接口
     */
    @FunctionalInterface
    private interface MaskingStrategy {
        String mask(String data);
    }
}
