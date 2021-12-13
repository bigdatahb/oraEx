package com.huangbo.oracle.export.utils;

/**
 * 工程: oracle_export
 * 包名: com.huangbo.oracle.export.utils
 * 创建日期: 2021/12/13
 * 作者: huangbo
 * Company:
 * version: 1.0.1
 * description:
 **/
public class StringUtil {

    public static boolean isEmpty(String value) {
        return null == value || value.length() == 0;
    }

    public static boolean isNotEmpty(String value) {
        return !isEmpty(value);
    }
}
