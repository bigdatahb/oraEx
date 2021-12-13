package com.huangbo.oracle.export.utils;

import org.apache.log4j.Logger;

import java.util.Map;

/**
 * 工程: oracle_export
 * 包名: com.huangbo.oracle.export.utils
 * 创建日期: 2021/12/13
 * 作者: huangbo
 * Company:
 * version: 1.0.1
 * description:
 **/
public class MapUtil {


    /**
     * 判断给定map是否为空
     *
     * @param map null 或者 size =0 定义为空
     * @return 空返回 true, 否则返回 FALSE
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.size() == 0;
    }

    /**
     * 判断给定map是否不为空
     *
     * @param map 给定 map
     * @return 判断标准同 isEmpty， 不为空返回 true
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }
}
