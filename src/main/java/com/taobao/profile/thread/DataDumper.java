package com.taobao.profile.thread;

import com.taobao.profile.Manager;
import com.taobao.profile.Profiler;
import com.taobao.profile.config.ProfConfig;
import com.taobao.profile.runtime.ProfStack;
import com.taobao.profile.runtime.ThreadData;
import com.taobao.profile.utils.DailyRollingFileWriter;
import com.taobao.profile.utils.StreamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * 将性能分析数据写到log中
 *
 * @author Michael.Wang
 * @date 2017/4/7
 */
public class DataDumper {
    private static Logger log = LoggerFactory.getLogger(DataDumper.class);
    /**
     * log writer
     */
    private DailyRollingFileWriter fileWriter;

    /**
     * log mysql writer
     */
    private DailyRollingFileWriter mysqlFileWriter;
    /**
     * 默认profile时间(s)
     */
    private int eachProfUseTime;
    /**
     * 两次profile间隔时间(s)
     */
    private int eachProfIntervalTime;

    private static DataDumper instance = new DataDumper(ProfConfig.getInstance());

    public static DataDumper getInstance() {
        return instance;
    }

    /**
     * 线程构造器
     *
     * @param config
     */
    public DataDumper(ProfConfig config) {
        // 读取用户配置
        log.debug("set log location to " + config.getLogFilePath());
        fileWriter = new DailyRollingFileWriter(config.getLogFilePath());
        File temp = new File(config.getLogFilePath());
        mysqlFileWriter = new DailyRollingFileWriter(temp.getParent() + "/mysqlProfiler.log");
        eachProfUseTime = config.getEachProfUseTime();
        eachProfIntervalTime = config.getEachProfIntervalTime();
    }

    public synchronized void dump()  {
        DailyRollingFileWriter writer = this.fileWriter;
        if (Manager.instance().canDump()) {
            log.debug("Now can dump.");
            Manager.instance().setProfileFlag(true); //开启采集
            try {
                TimeUnit.SECONDS.sleep(eachProfUseTime);
            } catch (InterruptedException e) {
                log.error("Sleeping for Profiling is interrupted.", e);
            }
            Manager.instance().setProfileFlag(false);  //结束采集
            try {
                TimeUnit.MILLISECONDS.sleep(500L); // 等待已开始的End方法执行完成
            } catch (InterruptedException e) {
                log.error("Sleeping for End is interrupted.",e);
            }
            dumpProfileData(writer);
            Manager.instance().setProfileFlag(true);//开启采集
            //dumpMysqlData();
        } else {
            log.warn("Now cannot dump.");
        }
    }

    /**
     * 将Profiler中收集到的数据写到log中
     *
     * @return
     */
    private void dumpProfileData(DailyRollingFileWriter fileWriter) {
        log.debug("call dumpProfileData [start]");
        ThreadData[] threadDataArray = Profiler.threadDataArray;
        if (threadDataArray == null || threadDataArray.length == 0) {
            log.warn("threadData is empty.");
        }
        for (int index = 0; index < threadDataArray.length; index++) {
            ThreadData profilerData = threadDataArray[index];
            if (profilerData == null) {
                continue;
            }
            ProfStack<long[]> profStack = profilerData.profileData;
            log.debug("start to prof stack. stack size is:" + profStack.size());
            while (profStack.size() > 0) {
                long[] data = profStack.pop();
                StringBuilder sb = new StringBuilder();
                sb.append(index).append('\t'); // thread id
                sb.append(data[1]).append('\t'); // stack number
                sb.append(data[0]).append('\t');// method id
                sb.append(data[2]).append('\n'); // use time
                fileWriter.append(sb.toString());
            }
            fileWriter.flushAppend();
            profilerData.clear();
        }
        fileWriter.append("=\n");
        fileWriter.flushAppend();
        log.debug("call dumpProfileData [end]");
    }

    public void close() {
        StreamUtil.close(this.fileWriter);
    }
}
