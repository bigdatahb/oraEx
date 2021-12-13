package com.huangbo.oracle.export.configs;

import com.huangbo.oracle.export.entity.Constant;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.Set;

/**
 * 工程: oracle_export
 * 包名: com.huangbo.oracle.export.configs
 * 创建日期: 2021/12/13
 * 作者: huangbo
 * Company:
 * version: 1.0.1
 * description:
 **/
public class DbConf {

    private static final Logger LOG = Logger.getLogger(DbConf.class);

    public static String oracleServer;

    public static String oraclePort;

    public static String oracleDatabase;

    public static String oracleUser;

    public static String oraclePassword;


    static {
        String dbConfFile = Constant.USER_DIR + Constant.FILE_SEPARATOR + "db.properties";
        System.out.println(dbConfFile);
        InputStream is = null;

        Properties pro = new Properties();
        try {
            is = new FileInputStream(dbConfFile);
            pro.load(new InputStreamReader(is, Charset.forName(Constant.ENCODE_UTF8)));

            Set<String> keys = pro.stringPropertyNames();
            for (String key : keys) {
                if ("oracle.server.ip".equals(key)) {
                    oracleServer = pro.getProperty(key);
                    continue;
                }
                if ("oracle.server.port".equals(key)) {
                    oraclePort = pro.getProperty(key);
                    continue;
                }
                if ("oracle.server.database".equals(key)) {
                    oracleDatabase = pro.getProperty(key);
                    continue;
                }
                if ("oracle.user".equals(key)) {
                    oracleUser = pro.getProperty(key);
                    continue;
                }
                if ("oracle.password".equals(key)) {
                    oraclePassword = pro.getProperty(key);
                }
            }
        } catch (IOException e) {
            LOG.error("读取配置文件 " + dbConfFile + " 异常", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LOG.error("关闭文件流异常", e);
                }
            }
        }
    }
}
