package com.huangbo.oracle.export.configs;

import com.google.gson.Gson;
import com.huangbo.oracle.export.entity.Constant;
import com.huangbo.oracle.export.entity.Export;
import com.huangbo.oracle.export.entity.ExportTable;
import com.huangbo.oracle.export.utils.CollectionUtil;
import com.huangbo.oracle.export.utils.MapUtil;
import com.huangbo.oracle.export.utils.StringUtil;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 工程: oracle_export
 * 包名: com.huangbo.oracle.export.entity
 * 创建日期: 2021/12/13
 * 作者: huangbo
 * Company:
 * version: 1.0.1
 * description:
 **/
public class ExportConf {

    private static final Logger LOG = Logger.getLogger(ExportConf.class);

    public static Integer bufferSize;

    public static String dataDir;

    public static Boolean tableHead;

    public static String splitter;

    public static String suffix;

    public static Map<String, List<ExportTable>> tableInfo;

    static {
        Gson gson = new Gson();
        String jsonFile = "export.json";
        try {
            Export json = gson.fromJson(new InputStreamReader(new FileInputStream(jsonFile), Charset.forName(Constant.ENCODE_UTF8)), Export.class);
            bufferSize = json.getBufferSize();
            dataDir = json.getDataDir();
            tableHead = json.getTableHead();
            splitter = json.getSplitter();
            suffix = json.getSuffix();
            tableInfo = json.getTableInfo();
            if (!checkConf()) {
                // 配置信息不正确, 退出程序
                System.exit(-1);
            }
        } catch (FileNotFoundException e) {
            LOG.error("找不到配置文件 " + jsonFile, e);
        }
    }

    private static boolean checkConf() {
        // 检查要导出的表信息配置
        if (MapUtil.isEmpty(tableInfo)) {
            LOG.error("未配置要导出的表信息");
            return false;
        }
        // 检查数据目录配置
        if (StringUtil.isEmpty(dataDir)) {
            LOG.info("未配置数据目录，使用当前目录下的 data 目录作为存放导出数据文件的根目录");
            dataDir = Constant.USER_DIR + Constant.FILE_SEPARATOR + "data";
            File file = new File(dataDir);
            if (!file.exists() || !file.isDirectory()) {
                LOG.info("目录 " + dataDir + " 不存在，创建目录...");
                if (!file.mkdirs()) {
                    LOG.error("创建目录 " + dataDir + " 失败！");
                    return false;
                }
            }
        }
        // 未配置表头
        if (null == tableHead) {
            tableHead = false;
        }

        // 未配置分隔符
        if (null == splitter) {
            splitter = ","; // 使用 "," 做默认分隔符
        }

        // 未配置文件后缀名
        if (null == suffix) {
            suffix = ".txt"; // 使用 ".txt" 用作默认后缀名
        }

        Set<String> users = tableInfo.keySet();
        // 创建各用户数据目录
        for (String user : users) {
            if (StringUtil.isEmpty(user)) {
                LOG.error("用户名不能为空");
                return false;
            }
            String userDir = dataDir + Constant.FILE_SEPARATOR + user;
            File ud = new File(userDir);
            if (!ud.exists() || !ud.isDirectory()) {
                if (ud.mkdirs()) {
                    LOG.info("创建用户数据目录: " + userDir + " 成功!");
                } else {
                    LOG.error("创建用户数据目录: " + userDir + " 失败!");
                    return false;
                }
            }

            // 获取该用户下的表配置信息
            List<ExportTable> tableList = tableInfo.get(user);
            if (CollectionUtil.isNotEmpty(tableList)) {
                for (ExportTable table : tableList) {
                    String tableName = table.getTableName();
                    String splitter = table.getSplitter();
                    String fileName = table.getFileName();
                    // 检查表名
                    if (StringUtil.isEmpty(tableName)) {
                        LOG.error("用户 " + user + " 下有未配置的 tableName");
                        return false;
                    }
                    // 检查分隔符
                    if (StringUtil.isEmpty(splitter)) {
                        // 配置为全局分隔符
                        table.setSplitter(ExportConf.splitter);
                    }
                    // 检查文件名
                    if (StringUtil.isEmpty(fileName)) {
                        if (StringUtil.isEmpty(suffix)) {
                            // 使用全局后缀
                            table.setFileName(table.getTableName() + ExportConf.suffix);
                        } else {
                            // 若配置了局部后缀，使用局部后缀
                            table.setFileName(table.getTableName() + suffix);
                        }
                    }
                }
            }
        }
        return true;
    }
}
