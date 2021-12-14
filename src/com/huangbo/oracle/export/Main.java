package com.huangbo.oracle.export;

import com.huangbo.oracle.export.service.ExportService;
import com.huangbo.oracle.export.utils.DbUtil;
import org.apache.log4j.Logger;

import java.text.NumberFormat;

/**
 * 工程: oracle_export
 * 包名: com.huangbo.oracle.export
 * 创建日期: 2021/12/13
 * 作者: huangbo
 * Company:
 * version: 1.0.1
 * description:
 **/
public class Main {
    /**
     * 日志对象
     */
    private static final Logger LOG = Logger.getLogger(Main.class);


    public static void main(String[] args) {
        try {
            long start = System.currentTimeMillis();
            ExportService es = ExportService.getInstance();
            // 导出表模式
            es.exportSchema();
            // 导出表数据
            es.exportData();
            long end = System.currentTimeMillis();
            LOG.info("导出任务完成，共耗时: " + humanizedTimeShow((end - start) / 1000));
        } catch (Exception e) {
            LOG.error("数据导出服务异常", e);
            System.exit(-1);
        } finally {
            // 关闭连接
            DbUtil.closeOracleConn();
        }
    }

    /**
     * 人性化显示时间
     *
     * @param seconds 时间秒数
     * @return 更为人性化的展示
     */
    private static String humanizedTimeShow(long seconds) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(2); // 设置最大小数位数
        if (seconds < 60)
            return seconds + " s";
        if (seconds < 3600)
            return nf.format(1.0 * seconds / 60) + " min";
        long hours = seconds / 3600;
        long secsRemaining = seconds % 3600;
        return hours + " h & " + humanizedTimeShow(secsRemaining);
    }
}
