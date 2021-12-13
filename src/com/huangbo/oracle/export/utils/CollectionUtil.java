package com.huangbo.oracle.export.utils;

import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * 工程: oracle_export
 * 包名: com.huangbo.oracle.export.utils
 * 创建日期: 2021/12/13
 * 作者: huangbo
 * Company:
 * version: 1.0.1
 * description:
 **/
public class CollectionUtil {


    /**
     * 判断集合是否为空
     *
     * @param c 集合
     * @return 集合为 null 或者 size为 0 返回true
     */
    public static boolean isEmpty(Collection<?> c) {
        return null == c || c.size() == 0;
    }

    public static boolean isNotEmpty(Collection<?> c) {
        return !isEmpty(c);
    }
}
