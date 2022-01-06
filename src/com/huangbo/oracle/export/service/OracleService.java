package com.huangbo.oracle.export.service;

import com.huangbo.oracle.export.configs.ExportConf;
import com.huangbo.oracle.export.entity.Column;
import com.huangbo.oracle.export.entity.Constant;
import com.huangbo.oracle.export.utils.CollectionUtil;
import com.huangbo.oracle.export.utils.DbUtil;
import com.huangbo.oracle.export.utils.StringUtil;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.*;

/**
 * 工程: oracle_export
 * 包名: com.huangbo.oracle.export.service
 * 创建日期: 2021/12/13
 * 作者: huangbo
 * Company:
 * version: 1.0.1
 * description: 单例模式
 **/
public class OracleService {
    /**
     * 日志对象
     */
    private static final Logger LOG = Logger.getLogger(OracleService.class);


    private Connection conn;

    private OracleService(Connection conn) {
        this.conn = conn;
    }

    private static class OracleServiceHolder {
        private static final OracleService INSTANCE = new OracleService(DbUtil.getOracleConn());
    }

    public static OracleService getInstance() {
        return OracleServiceHolder.INSTANCE;
    }

    /**
     * 打印结果集至标准输出
     *
     * @param result 结果集
     */
    public void printResultSet(ResultSet result) {
        try {
            ResultSetMetaData metaData = result.getMetaData();
            int count = metaData.getColumnCount();
            // 打印表头
            for (int i = 1; i <= count; ++i) {
                System.out.print(String.format("%-32s", metaData.getColumnName(i)));
            }
            System.out.println();
            // 打印数据
            while (result.next()) {
                for (int i = 1; i <= count; ++i) {
                    System.out.print(String.format("%-32s", result.getObject(i)));
                }
                System.out.println();
            }
        } catch (SQLException e) {
            LOG.error("", e);
        }
    }

    /**
     * 判断指定用户是否存在
     *
     * @param user 用户名
     * @return 存在返回 true, 否则返回 FALSE
     */
    public boolean isUserExists(String user) {
        String sql = "SELECT COUNT(*) FROM ALL_USERS WHERE USERNAME='" + user.toUpperCase() + "'";
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(sql);
            if (result.next()) {
                return result.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOG.error("执行SQL " + sql + " 异常", e);
        } finally {
            DbUtil.closeStatement(stmt);
        }
        return false;
    }

    /**
     * 判断表是否存在
     *
     * @param user  用户名
     * @param table 表名
     * @return 存在返回 true, 否则返回 false
     */
    public boolean isTableExists(String user, String table) {
        user = user.toUpperCase();
        table = table.toUpperCase();
        String sql = "SELECT COUNT(*) FROM ALL_TABLES WHERE OWNER='" + user + "' AND TABLE_NAME='" + table + "'";
        Statement stmt = null;
        ResultSet result = null;
        try {
            stmt = conn.createStatement();
            result = stmt.executeQuery(sql);
            if (result.next()) {
                return result.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOG.error("执行SQL " + sql + " 异常", e);
        } finally {
            DbUtil.closeResultSetAndStatement(result, stmt);
        }
        return false;
    }

    /**
     * 获取指定用户下的所有表
     *
     * @param user 用户
     * @return 归属于指定用户的表集合
     */
    public Set<String> getUserTables(String user) {
        Set<String> tables = new HashSet<>();
        String sql = "select table_name from all_tables where owner=upper('" + user + "')";
        Statement stmt = null;
        ResultSet result = null;
        try {
            stmt = conn.createStatement();
            result = stmt.executeQuery(sql);
            while (result.next()) {
                tables.add(result.getString(1));
            }
        } catch (SQLException e) {
            LOG.error("执行SQL " + sql + " 异常", e);
        } finally {
            DbUtil.closeResultSetAndStatement(result, stmt);
        }
        return tables;
    }

    /**
     * 获取表注释
     *
     * @param user  用户名
     * @param table 表名
     * @return 表注释
     */
    public String getTableComment(String user, String table) {
        String sql = "SELECT COMMENTS FROM ALL_TAB_COMMENTS WHERE OWNER='" + user.toUpperCase() + "' AND TABLE_NAME = '" + table.toUpperCase() + "'";
        Statement stmt = null;
        ResultSet result = null;
        try {
            stmt = conn.createStatement();
            result = stmt.executeQuery(sql);
            if (result.next()) {
                return result.getString(1);
            }
        } catch (SQLException e) {
            LOG.error("执行SQL " + sql + " 异常", e);
        } finally {
            DbUtil.closeResultSetAndStatement(result, stmt);
        }
        return "";
    }

    /**
     * 获取指定用户指定表的字段信息
     *
     * @param user  用户名
     * @param table 表名
     * @return 字段信息列表
     */
    public List<Column> getColumnInfo(String user, String table) {
        user = user.toUpperCase();
        table = table.toUpperCase();
        List<Column> list = new ArrayList<>();
        String sql = "SELECT \n" +
                "\tA.COLUMN_NAME, \n" +
                "\tA.DATA_TYPE,\n" +
                "\tA.DATA_PRECISION,\n" +
                "\tA.DATA_SCALE,\n" +
                "\tA.NULLABLE,\n" +
                "\tA.COLUMN_ID,\n" +
                "\tB.COMMENTS\n" +
                "FROM (\n" +
                "\tSELECT COLUMN_NAME, DATA_TYPE, DATA_PRECISION, DATA_SCALE, NULLABLE, COLUMN_ID FROM ALL_TAB_COLS WHERE OWNER='" + user + "' AND TABLE_NAME='" + table + "' AND COLUMN_ID IS NOT NULL\n" +
                ") A \n" +
                "LEFT JOIN \n" +
                "\t(SELECT COLUMN_NAME, COMMENTS FROM ALL_COL_COMMENTS WHERE OWNER='" + user + "' AND TABLE_NAME='" + table + "') B\n" +
                "ON \n" +
                "\tA.COLUMN_NAME = B.COLUMN_NAME\n" +
                "ORDER BY \n" +
                "\tCOLUMN_ID";
        Statement stmt = null;
        ResultSet result = null;
        try {
            stmt = conn.createStatement();
            result = stmt.executeQuery(sql);
            while (result.next()) {
                // 字段名
                String name = result.getString(1);
                String dataType = result.getString(2);
                int dataPrecision = result.getInt(3);
                int dataScale = result.getInt(4);
                boolean nullable = "Y".equals(result.getString(5));
                int position = result.getInt(6);
                String comment = result.getString(7);

                // 数据类型转换
                String type = dataTypeTransferToOdps(dataType, dataPrecision, dataScale);

                Column column = new Column(name, type);
                column.setComment(comment);
                column.setPosition(position);
                column.setNullable(nullable);
                list.add(column);
            }
        } catch (SQLException e) {
            LOG.error("执行SQL: " + sql + " 异常", e);
        } finally {
            DbUtil.closeResultSetAndStatement(result, stmt);
        }
        return list;
    }

    /**
     * 写数据至文件
     *
     * @param user     用户名
     * @param table    表名
     * @param outFile  导出文件名
     * @param splitter 分隔符
     * @return 正常写入返回 true , 否则返回 false
     */
    public boolean writeDataToFile(String user, String table, String outFile, String splitter) {
        if (!isUserExists(user)) {
            LOG.error("用户 " + user + " 不存在");
            return false;
        }
        if (!isTableExists(user, table)) {
            LOG.error("表 " + user + "." + table + " 不存在!");
            return false;
        }
        File file = new File(outFile);
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    LOG.info("创建数据文件 [" + outFile + " ]成功， 正在导出数据...");
                }
            } catch (IOException e) {
                LOG.error("创建文件失败: " + outFile, e);
                return false;
            }
        } else {
            LOG.info("文件 [" + outFile + " ] 已存在！");
            return false;
        }

        String sql = "SELECT * FROM " + user.toUpperCase() + "." + table.toUpperCase();

        Statement stmt = null;
        ResultSet result = null;
        BufferedWriter bw = null;
        int cnt = 0;

        try {
            stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            // 关闭自动提交
            conn.setAutoCommit(false);
            stmt.setFetchDirection(ResultSet.FETCH_FORWARD);
            stmt.setFetchSize(ExportConf.bufferSize);
            result = stmt.executeQuery(sql);
            if (result == null) {
                return true;
            }
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), Charset.forName(Constant.ENCODE_UTF8)));
            // 判断是否是 csv 格式
            boolean isCsvFile = outFile.toLowerCase().endsWith(".csv");
            ResultSetMetaData metaData = result.getMetaData();
            int count = metaData.getColumnCount();
            StringBuilder buffer = new StringBuilder();

            if (ExportConf.tableHead) {
                // 写表头
                for (int i = 1; i < count; ++i) {
                    buffer.append(metaData.getColumnName(i)).append(splitter);
                }
                buffer.append(metaData.getColumnName(count)).append(Constant.LINE_SEPARATOR);
                bw.write(buffer.toString());
                buffer.delete(0, buffer.length()); // 清空buffer
            }

            result.setFetchDirection(ResultSet.FETCH_FORWARD);
//            result.setFetchSize(ExportConf.bufferSize);

            while (result.next()) {
                ++cnt;
                for (int i = 1; i <= count; ++i) {
                    String type = metaData.getColumnTypeName(i);
                    int bSize = 4096; // 由于 blob 类型的值可能非常大，这里设置一个每次读取的步长值
                    // blob 类型
                    if (isBlobType(type)) {
                        Blob blob = result.getBlob(i);
                        long size = blob.length();
                        int pos = 1;
                        while (pos <= size) {
                            byte[] buf = blob.getBytes(pos, bSize);
                            pos += bSize;
                            String val = new String(buf, Constant.ENCODE_UTF8);
                            if (isCsvFile) {
                                // csv格式清理
                                val = StringUtil.csvClean(val, splitter);
                            } else {
                                // txt 格式
                                val = StringUtil.textClean(val, splitter);
                            }
                            buffer.append(val);
                        }
                        // 释放资源
                        blob.free();
                        buffer.append(splitter);
                        continue;
                    }
                    // clob 类型
                    if (isClobType(type)) {
                        Clob clob = result.getClob(i);
                        String s = clob2String(clob);
                        if (isCsvFile) {
                            s = StringUtil.csvClean(s, splitter);
                        } else {
                            s = StringUtil.textClean(s, splitter);
                        }
                        clob.free();
                        buffer.append(s).append(splitter);
                        continue;
                    }
                    // 获取一般值, 这里不再做列类型判定，将所有值都转为字符串类型
                    Object value = result.getObject(i);
                    if (null == value) {
                        buffer.append(splitter);
                        continue;
                    }
                    String val = String.valueOf(value);
                    if (isCsvFile) {
                        val = StringUtil.csvClean(val, splitter);
                    } else {
                        val = StringUtil.textClean(val, splitter);
                    }
                    buffer.append(val).append(splitter);
                }
                // 删除最后一个分隔符
                buffer.delete(buffer.lastIndexOf(splitter), buffer.length());
                // 添加换行符
                buffer.append(Constant.LINE_SEPARATOR);
                if (cnt % ExportConf.bufferSize == 0) {
                    // 写入 buffer
                    bw.write(buffer.toString());
                    // 清空 buffer
                    buffer.delete(0, buffer.length());
                }
            }
            if (buffer.length() > 0) {
                // 删除最后一个换行符
                buffer.delete(buffer.lastIndexOf(Constant.LINE_SEPARATOR), buffer.length());
                bw.write(buffer.toString());
                buffer.delete(0, buffer.length());
            }
        } catch (Exception e) {
            LOG.error("导出数据异常 [" + sql + " ], 导出行数: " + cnt, e);
            try {
                LOG.info("连接状态: closed? : " + conn.isClosed() + ", valid? : " + conn.isValid(10), e);
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            // 重新打开 oracle 连接
            DbUtil.closeOracleConn();
            this.conn = DbUtil.getOracleConn();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    LOG.error("关闭文件流异常", e);
                }
            }
            DbUtil.closeResultSetAndStatement(result, stmt);
        }
        return false;
    }

    public String createSqlOdps(String user, String table) {
        List<Column> columnList = getColumnInfo(user, table);
        if (CollectionUtil.isEmpty(columnList)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ").append(user).append("_").append(table).append("(")
                .append(Constant.LINE_SEPARATOR);
        for (Column col : columnList) {
            String name = col.getName();
            // 清洗列名称， 比如处理列名中的 ( 或者 . 等特殊字符
            name = cleanName(name);
            String type = col.getType();
            String comment = col.getComment();
            if (StringUtil.isEmpty(comment)) {
                comment = "";
            }
            boolean nullable = col.getNullable();
            if (!nullable) {
                type = type + " NOT NULL";
            }
            sb.append('\t').append(name).append(" ").append(type)
                    .append(" COMMENT '").append(comment).append("',")
                    .append(Constant.LINE_SEPARATOR);
        }
        // 删除最后一个逗号
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append(")");
        String tableComment = getTableComment(user, table);
        if (StringUtil.isNotEmpty(tableComment)) {
            sb.append(" COMMENT '").append(tableComment).append("'")
                    .append(Constant.LINE_SEPARATOR);
        }
        sb.append(";").append(Constant.LINE_SEPARATOR);
        return sb.toString();
    }

    /**
     * 处理列名中的特殊字符
     *
     * @param name 列名称
     * @return 处理后的列名称
     */
    private String cleanName(String name) {
        if (name.contains("(") && name.contains(")")) {
            name = name.substring(name.indexOf("(") + 1, name.indexOf(")"));
        }
        if (name.contains(".")) {
            name = name.substring(name.indexOf(".") + 1);
        }
        return name;
    }

    /**
     * clob 值转字符串
     *
     * @param clob clob 对象
     * @return clob值对应的字符串
     */
    private String clob2String(Clob clob) {
        BufferedReader br = null;
        StringBuilder res = new StringBuilder();

        try {
            Reader rd = clob.getCharacterStream();
            br = new BufferedReader(rd);
            String line;
            while ((line = br.readLine()) != null) {
                res.append(line).append(Constant.LINE_SEPARATOR);
            }
            if (res.length() > Constant.LINE_SEPARATOR.length()) {
                // 去掉最后一个换行符
                res.delete(res.lastIndexOf(Constant.LINE_SEPARATOR), res.length());
            }
        } catch (SQLException e) {
            LOG.error("获取clob的值异常", e);
        } catch (IOException e) {
            LOG.error("读取clob的值异常", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    LOG.error("关闭读取流异常", e);
                }
            }
        }
        return res.toString();
    }

    /**
     * 将oracle数据类型转换为 MaxCompute 数据类型
     *
     * @param dataType      oracle 数据类型
     * @param dataPrecision 数值类型精度
     * @param dataScale     数值类型范围
     * @return MaxCompute 对应类型
     */
    private String dataTypeTransferToOdps(String dataType, int dataPrecision, int dataScale) {
        // 字符类型
        if (isCharType(dataType) || isVarcharType(dataType)
                || isClobType(dataType) || isBlobType(dataType)) {
            return Constant.DATA_TYPE_STRING;
        }
        // 数值类型
        if (Constant.DATA_TYPE_NUMBER.equalsIgnoreCase(dataType)) {
            // number 类型
            if (dataScale <= 0) {
                if (dataPrecision == 0) {
                    // 没有指定精度
                    return "DECIMAL(16,2)";
                }
                if (dataPrecision <= 8) {
                    // 整型
                    return Constant.DATA_TYPE_INT;
                }
                // 大整型
                return Constant.DATA_TYPE_BIGINT;
            } else {
                if (dataPrecision <= dataScale) {
                    return Constant.DATA_TYPE_BIGINT;
                }
                // 小数
                return "DECIMAL(" + dataPrecision + ", " + dataScale + ")";
            }
        }
        // 日期类型
        if (Constant.DATA_TYPE_DATE.equalsIgnoreCase(dataType)) {
            return Constant.DATA_TYPE_DATETIME;
        }
        return dataType;
    }

    /**
     * 判断是否是 CHAR 相关类型
     *
     * @param dataType oracle 数据类型名称
     * @return 是 CHAR 类型返回 true , 否则返回 false
     */
    private boolean isCharType(String dataType) {
        return Constant.DATA_TYPE_CHAR.equals(dataType) || Constant.DATA_TYPE_NCHAR.equals(dataType);
    }

    /**
     * 判断是否是 VARCHAR 相关类型
     *
     * @param dataType oracle 数据类型
     * @return 是 VARCHAR 类型返回 true, 否则返回 false
     */
    private boolean isVarcharType(String dataType) {
        return Constant.DATA_TYPE_VARCHAR.equals(dataType) || Constant.DATA_TYPE_VARCHAR2.equals(dataType)
                || Constant.DATA_TYPE_NVARCHAR2.equals(dataType);
    }

    /**
     * 判断是否是 CLOB 相关类型
     *
     * @param dataType 数据类型
     * @return 是 clob 类型返回 true, 否则返回 false
     */
    private boolean isClobType(String dataType) {
        return Constant.DATA_TYPE_CLOB.equalsIgnoreCase(dataType)
                || Constant.DATA_TYPE_NCLOB.equalsIgnoreCase(dataType);
    }

    /**
     * 判断是否是 BLOB 类型
     *
     * @param dataType 数据类型
     * @return
     */
    private boolean isBlobType(String dataType) {
        return Constant.DATA_TYPE_BLOB.equalsIgnoreCase(dataType);
    }
}
