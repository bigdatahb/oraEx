package com.huangbo.oracle.export.utils;

import com.huangbo.oracle.export.configs.DbConf;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.Properties;

/**
 * 工程: oracle_export
 * 包名: com.huangbo.oracle.export.utils
 * 创建日期: 2021/12/13
 * 作者: huangbo
 * Company:
 * version: 1.0.1
 * description:
 **/
public class DbUtil {
    /**
     * 日志对象
     */
    private static final Logger LOG = Logger.getLogger(DbUtil.class);

    private static final String oracleDriver = "oracle.jdbc.OracleDriver";

    private static Connection oracleConn = null;

    private static String oracleUrl = "jdbc:oracle:thin:@//" + DbConf.oracleServer + ":" + DbConf.oraclePort + "/" + DbConf.oracleDatabase;

    private DbUtil() {

    }

    private static void connectOracle() {
        if (oracleConn == null) {
            try {
                Class.forName(oracleDriver);
                Properties conf = new Properties();
                conf.setProperty("user", DbConf.oracleUser);
                conf.setProperty("password", DbConf.oraclePassword);
                conf.setProperty("oracle.jdbc.ReadTimeout", "600000");
                oracleConn = DriverManager.getConnection(oracleUrl, conf);
            } catch (ClassNotFoundException e) {
                LOG.error("加载驱动 " + oracleDriver + " 异常", e);
            } catch (SQLException e) {
                LOG.error("获取Oracle连接异常", e);
            }

        }
    }

    public static Connection getOracleConn() {
        if (oracleConn == null) {
            connectOracle();
        }
        return oracleConn;
    }

    public static void closeOracleConn() {
        if (oracleConn != null) {
            try {
                oracleConn.close();
                oracleConn = null;
            } catch (SQLException e) {
                LOG.error("关闭Oracle连接异常", e);
            }
        }
    }

    public static void closeResultSet(ResultSet result) {
        if (result != null) {
            try {
                result.close();
            } catch (SQLException e) {
                LOG.error("关闭结果集异常", e);
            }
        }
    }

    public static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                LOG.error("关闭 statement 异常", e);
            }
        }
    }

    public static void closeResultSetAndStatement(ResultSet result, Statement stmt) {
        closeResultSet(result);
        closeStatement(stmt);
    }
}
