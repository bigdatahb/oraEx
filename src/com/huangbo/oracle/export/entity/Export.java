package com.huangbo.oracle.export.entity;

import java.util.List;
import java.util.Map;

/**
 * 工程: oracle_export
 * 包名: com.huangbo.oracle.export.entity
 * 创建日期: 2021/12/13
 * 作者: huangbo
 * Company:
 * version: 1.0.1
 * description:
 **/
public class Export {

    private  Integer bufferSize;

    private  String dataDir;

    private  Boolean tableHead;

    private  String splitter;

    private  String suffix;

    private Map<String, List<ExportTable>> tableInfo;

    public Integer getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(Integer bufferSize) {
        this.bufferSize = bufferSize;
    }

    public String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public Boolean getTableHead() {
        return tableHead;
    }

    public void setTableHead(Boolean tableHead) {
        this.tableHead = tableHead;
    }

    public String getSplitter() {
        return splitter;
    }

    public void setSplitter(String splitter) {
        this.splitter = splitter;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public Map<String, List<ExportTable>> getTableInfo() {
        return tableInfo;
    }

    public void setTableInfo(Map<String, List<ExportTable>> tableInfo) {
        this.tableInfo = tableInfo;
    }
}
