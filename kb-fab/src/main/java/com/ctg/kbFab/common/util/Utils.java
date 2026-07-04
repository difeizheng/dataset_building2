package com.ctg.kbFab.common.util;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 工具类
 *
 * @author Developer
 * @since 2026-07-01
 */
public class Utils {

    /**
     * 生成唯一ID
     */
    public static String generateId() {
        return IdUtil.getSnowflakeNextIdStr();
    }

    /**
     * 判断字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return StrUtil.isEmpty(str);
    }

    /**
     * 判断字符串是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return StrUtil.isNotEmpty(str);
    }

    /**
     * 截取字符串
     */
    public static String sub(String str, int from, int to) {
        return StrUtil.sub(str, from, to);
    }
}
