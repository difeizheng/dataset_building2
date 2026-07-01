package com.ctg.dataFab.common.util;

import java.util.UUID;

/**
 * 工具类
 *
 * @author Developer
 * @since 2026-07-01
 */
public class Utils {

    /**
     * 生成UUID
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成TraceId
     */
    public static String generateTraceId() {
        return "trace-" + generateUUID();
    }
}
