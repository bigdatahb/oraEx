package com.huangbo.oracle.export.service;


import com.huangbo.oracle.export.configs.ExportConf;
import com.huangbo.oracle.export.entity.Constant;
import com.huangbo.oracle.export.entity.ExportTable;
import com.huangbo.oracle.export.utils.CollectionUtil;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 工程: oracle_export
 * 包名: com.huangbo.oracle.export.service
 * 创建日期: 2021/12/13
 * 作者: huangbo
 * Company:
 * version: 1.0.1
 * description:
 **/
public class ExportService {
    /**
     * 日志对象
     */
    private static final Logger LOG = Logger.getLogger(ExportService.class);

    private OracleService os = OracleService.getInstance();

    private ExportService() {
        standardExportInfo();
    }


    private static class ExportServiceHolder {
        private static final ExportService INSTANCE = new ExportService();
    }

    public static ExportService getInstance() {
        return ExportServiceHolder.INSTANCE;
    }

    /**
     * 导出数据
     */
    public void exportData() {
        Map<String, List<ExportTable>> map = ExportConf.tableInfo;
        for (String user : map.keySet()) {
            List<ExportTable> tableList = map.get(user);
            LOG.info("开始导出用户 [" + user + "] 的数据, 共 " + tableList.size() + " 张表");
            for (ExportTable table : tableList) {
                String tableName = table.getTableName();
                LOG.info("开始导出表 [" + user + "." + tableName + "] 的数据...");
                String userDir = ExportConf.dataDir + Constant.FILE_SEPARATOR + user;
                String outFile = userDir + Constant.FILE_SEPARATOR + table.getFileName();
                if (os.writeDataToFile(user, tableName, outFile, table.getSplitter())) {
                    LOG.info("表 [" + user + "." + tableName + "] 导出完毕, 导出文件: " + outFile);
                }
            }
            LOG.info("用户 [" + user + "] 的数据导出完毕!");
        }
    }

    /**
     * 导出建表模式
     */
    public void exportSchema() {
        String sqlDir = ExportConf.dataDir + Constant.FILE_SEPARATOR + "sql";
        File dir = new File(sqlDir);
        if (!dir.exists() || !dir.isDirectory()) {
            if (dir.mkdirs()) {
                LOG.info("创建目录 " + sqlDir + " 成功!");
            } else {
                LOG.error("创建目录 " + sqlDir + " 失败!");
                return;
            }
        }
        Map<String, List<ExportTable>> map = ExportConf.tableInfo;
        for (String user : map.keySet()) {
            String outFile = sqlDir + Constant.FILE_SEPARATOR + user + ".sql";
            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), Charset.forName(Constant.ENCODE_UTF8)));
                StringBuilder buffer = new StringBuilder();
                List<ExportTable> tableList = map.get(user);
                for (ExportTable table : tableList) {
                    String createSql = table.getCreateSql();
                    buffer.append(createSql).append(Constant.LINE_SEPARATOR);
                }
                bw.write(buffer.toString());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                LOG.error("写文件 " + outFile + " 异常", e);
            } finally {
                if (bw != null) {
                    try {
                        bw.close();
                    } catch (IOException e) {
                        LOG.error("关闭文件 " + outFile + " 异常", e);
                    }
                }
            }

        }
    }

    /**
     * 标准化导出信息(信息补全)
     */
    private void standardExportInfo() {
        Map<String, List<ExportTable>> tableInfo = ExportConf.tableInfo;
        Set<String> users = tableInfo.keySet();
        for (String user : users) {
            List<ExportTable> tables = tableInfo.get(user);
            if (CollectionUtil.isNotEmpty(tables)) {
                // 用户配置了要导出的表的信息
                for (ExportTable table : tables) {
                    // 获取建表语句
                    String createSql = os.createSqlOdps(user, table.getTableName());
                    table.setCreateSql(createSql);
                }
            } else {
                tables = new ArrayList<>();
                // 未配置表信息，需要导出该用户下的所有表
                Set<String> tableNames = os.getUserTables(user);
                for (String tname : tableNames) {
                    ExportTable table = new ExportTable();
                    // 设置表名
                    table.setTableName(tname);
                    // 设置表对应的文件名
                    table.setFileName(tname + ExportConf.suffix);
                    // 设置分隔符
                    table.setSplitter(ExportConf.splitter);
                    // 设置建表语句
                    String createSql = os.createSqlOdps(user, tname);
                    table.setCreateSql(createSql);
                    // 添加表
                    tables.add(table);
                }
                tableInfo.put(user, tables);
            }
        }
    }
}
