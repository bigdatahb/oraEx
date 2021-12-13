package com.huangbo.oracle.export.entity;

/**
 * 工程: oracle_export
 * 包名: com.huangbo.oracle.export.entity
 * 创建日期: 2021/12/13
 * 作者: huangbo
 * Company:
 * version: 1.0.1
 * description:
 **/
public interface Constant {
    // 用户目录
    String USER_DIR = System.getProperty("user.dir");
    // 换行符
    String LINE_SEPARATOR = System.lineSeparator();
    // 文件路径分隔符
    String FILE_SEPARATOR = System.getProperty("file.separator");
    // UTF-8 编码
    String ENCODE_UTF8 = "UTF-8";
    // GBK 编码
    String ENCODE_GBK = "GBK";


    // ------------  定义 ORACLE 数据类型 --------------
    String DATA_TYPE_CHAR = "CHAR";

    String DATA_TYPE_NCHAR = "NCHAR";

    String DATA_TYPE_NUMBER = "NUMBER";

    String DATA_TYPE_VARCHAR2 = "VARCHAR2";

    String DATA_TYPE_VARCHAR = "VARCHAR";

    String DATA_TYPE_NVARCHAR2 = "NVARCHAR2";

    String DATA_TYPE_DATE = "DATE";

    String DATA_TYPE_CLOB = "CLOB";

    String DATA_TYPE_BLOB = "BLOB";

    String DATA_TYPE_NCLOB = "NCLOB";

    // --------------- 定义 odps 数据类型 -----------------
    String DATA_TYPE_DATETIME = "DATETIME";

    String DATA_TYPE_INT = "INT";

    String DATA_TYPE_BIGINT = "BIGINT";

    String DATA_TYPE_STRING = "STRING";

}
