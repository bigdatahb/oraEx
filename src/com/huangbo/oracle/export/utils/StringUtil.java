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

    /**
     * 将字符串清洗成 csv 格式
     *
     * @param val      要清洗的字符串
     * @param splitter 分隔符
     * @return 清洗结果
     */
    public static String csvClean(String val, String splitter) {
        if (StringUtil.isEmpty(val)) {
            return "";
        }
        val = commonClean(val);
        // 处理 \n
        while (val.contains("\n")) {
            int index = val.indexOf("\n");
            String pre = val.substring(0, index);
            String post = val.substring(index + "\n".length());
            // 将 \n 转换成系统换行符
            val = pre + System.lineSeparator() + post;
        }
        // 处理双引号
        if (val.contains("\"")) {
            StringBuilder sb = new StringBuilder(val);
            int pos = 0;
            while ((pos = sb.indexOf("\"", pos)) != -1) {
                sb.insert(pos, "\"");
                pos = sb.indexOf("\"", pos + 2);
                if (pos >= sb.length() || pos == -1) {
                    break;
                }
            }
            val = sb.toString();
        }

        // 分隔符处理
        if (val.contains(splitter)) {
            val = "\"" + val + "\"";
        }
        return val;
    }

    /**
     * 将字符串清洗成 txt 格式
     *
     * @param val      要清洗的字符串
     * @param splitter 分隔符
     * @return 清洗结果
     */
    public static String textClean(String val, String splitter) {
        if (StringUtil.isEmpty(val)) {
            return "";
        }
        val = commonClean(val);
        // 处理 \n
        while (val.contains("\n")) {
            int index = val.indexOf("\n");
            String pre = val.substring(0, index);
            String post = val.substring(index + "\n".length());
            // 因为 txt 格式行终止符为换行符，因此这里需要将换行符去掉或者替换为其他符号
            val = pre + " " + post;
        }


        // 处理转义字符
        if (val.contains("\\")) {
            StringBuilder sb = new StringBuilder(val);
            int pos = 0;
            while ((pos = val.indexOf("\\")) != -1) {
                sb.insert(pos, "\\");
                pos = sb.indexOf("\\", pos + 2);
                if (pos >= sb.length() || pos == -1) {
                    break;
                }
            }
            val = sb.toString();
        }

        // 分隔符处理
        if (val.contains(splitter)) {
            // 在分隔符前面加上转义字符
            StringBuilder sb = new StringBuilder(val);
            int pos = 0;
            while ((pos = sb.indexOf(splitter)) != -1) {
                sb.insert(pos, "\\"); // 插入转义字符
                pos = sb.indexOf(splitter, pos + 2);
                if (pos >= sb.length() || pos == -1) {
                    break;
                }
            }
            val = sb.toString();
        }
        return val;
    }


    /**
     * 一般公共清洗, 无论何种格式都需要做的清洗
     *
     * @param val 要清洗的字符串
     * @return 清洗结果
     */
    private static String commonClean(String val) {
        val = val.trim();
        // 去除 \r 字符 , 之所以将 \r 与 \n 分开处理，是因为有的值中存在只含有 \r 的情况, 其实可以使用 replaceAll 方法
        while (val.contains("\r")) {
            int index = val.indexOf("\r");
            String pre = val.substring(0, index);
            String post = val.substring(index + "\r".length());
            // 去除 \r
            val = pre + post;
        }

        // 去掉空字符
        while (val.indexOf('\0') != -1) {
            int pos = val.indexOf('\0');
            String pre = val.substring(0, pos);
            String post = "";
            if (pos < val.length()) {
                post = val.substring(pos + 1, val.length());
            }
            val = pre + post;
        }
        return val;
    }
}
