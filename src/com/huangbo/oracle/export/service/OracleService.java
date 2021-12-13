package com.huangbo.oracle.export.service;

import com.huangbo.oracle.export.entity.Column;
import com.huangbo.oracle.export.entity.Constant;
import com.huangbo.oracle.export.utils.DbUtil;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        String sql = "select count(*) from all_users where username='" + user.toUpperCase() + "'";
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
     * 获取指定用户指定表的字段信息
     *
     * @param user  用户名
     * @param table 表名
     * @return 字段信息列表
     */
    public List<Column> getColumnInfo(String user, String table) {
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
                "\tSELECT COLUMN_NAME, DATA_TYPE, DATA_PRECISION, DATA_SCALE, NULLABLE, COLUMN_ID FROM ALL_TAB_COLS WHERE OWNER='YIBAO' AND TABLE_NAME='DINGZHOU_FUYY_住院费用' AND COLUMN_ID IS NOT NULL\n" +
                ") A \n" +
                "LEFT JOIN \n" +
                "\t(SELECT COLUMN_NAME, COMMENTS FROM ALL_COL_COMMENTS WHERE OWNER='YIBAO' AND TABLE_NAME='DINGZHOU_FUYY_住院费用') B\n" +
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
        }finally {
            DbUtil.closeResultSetAndStatement(result, stmt);
        }
        return list;
    }



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
