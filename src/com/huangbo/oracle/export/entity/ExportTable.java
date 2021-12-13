package com.huangbo.oracle.export.entity;

import org.apache.log4j.Logger;

/**
 * 工程: oracle_export
 * 包名: com.huangbo.oracle.export.entity
 * 创建日期: 2021/12/13
 * 作者: huangbo
 * Company:
 * version: 1.0.1
 * description:
 **/
public class ExportTable {

    private static final Logger LOG = Logger.getLogger(ExportTable.class);
    /**
     * 表名
     */
    private String tableName;
    /**
     * 导出数据文件名
     */
    private String fileName;
    /**
     * 导出数据文件分隔符
     */
    private String splitter;
    /**
     * 建表语句
     */
    private String createSql;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSplitter() {
        return splitter;
    }

    public void setSplitter(String splitter) {
        this.splitter = splitter;
    }

    public String getCreateSql() {
        return createSql;
    }

    public void setCreateSql(String createSql) {
        this.createSql = createSql;
    }

    @Override
    public String toString() {
        return "ExportTable{" +
                "tableName='" + tableName + '\'' +
                ", fileName='" + fileName + '\'' +
                ", splitter='" + splitter + '\'' +
                ", createSql='" + createSql + '\'' +
                '}';
    }
}
