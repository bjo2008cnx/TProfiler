package com.taobao.profile.thread;

import com.taobao.profile.Manager;
import com.taobao.profile.Profiler;
import com.taobao.profile.config.ProfConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 将性能分析数据写到log中
 *
 * @author shutong.dy
 * @since 2012-1-11
 */
public class DataDumpThread extends Thread {
    private static Logger log = LoggerFactory.getLogger(DataDumpThread.class);

    public void run() {
        log.info("DataDumpThread is starting.............");
        try {
            while (true) {
                DataDumper.getInstance().dump();
                TimeUnit.SECONDS.sleep(ProfConfig.getInstance().getEachProfIntervalTime());
            }
        } catch (Exception e) {
            log.error("Thread fail to start", e);
        } finally {
            clear();
        }
        log.info("DataDumpThread is started ");
    }

    private void clear() {
        Manager.instance().setProfileFlag(false);
        DataDumper.getInstance().close();

        // 等待已开始的End方法执行完成
        try {
            TimeUnit.MILLISECONDS.sleep(500L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Profiler.clearData();
    }
}
