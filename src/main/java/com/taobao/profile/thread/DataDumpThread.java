/**
 * (C) 2011-2012 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 *
 */
package com.taobao.profile.thread;

import com.taobao.profile.Manager;
import com.taobao.profile.Profiler;
import com.taobao.profile.config.ProfConfig;
import com.taobao.profile.dependence_query.RecordSlowQuery;
import com.taobao.profile.dependence_query.SlowQueryData;
import com.taobao.profile.runtime.ProfStack;
import com.taobao.profile.runtime.ThreadData;
import com.taobao.profile.utils.DailyRollingFileWriter;
import com.taobao.profile.utils.ThreadUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * 将性能分析数据写到log中
 *
 * @author shutong.dy
 * @since 2012-1-11
 */
@Slf4j
public class DataDumpThread extends Thread {
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

    /**
     * 线程构造器
     *
     * @param config
     */
    public DataDumpThread(ProfConfig config) {
        // 读取用户配置
        fileWriter = new DailyRollingFileWriter(config.getLogFilePath());
        File temp = new File(config.getLogFilePath());
        mysqlFileWriter = new DailyRollingFileWriter(temp.getParent() + "/mysqlProfiler.log");
        eachProfUseTime = config.getEachProfUseTime();
        eachProfIntervalTime = config.getEachProfIntervalTime();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Thread#run()
     */
    public void run() {
        try {
            while (true) {
                dump();
                TimeUnit.SECONDS.sleep(eachProfIntervalTime);
            }
        } catch (Exception e) {
            log.error("error", e);
        } finally {
            Manager.instance().setProfileFlag(false);
            if (fileWriter != null) {
                fileWriter.closeFile();
            }
            ThreadUtil.sleep(500); // 等待已开始的End方法执行完成
            Profiler.clearData();
        }
    }

    private void dump() throws InterruptedException {
        if (Manager.instance().canDump()) {
            Manager.instance().setProfileFlag(true);
            TimeUnit.SECONDS.sleep(eachProfUseTime);
            Manager.instance().setProfileFlag(false);
            ThreadUtil.sleep(500);  // 等待已开始的End方法执行完成
            dumpProfileData();
            dumpMysqlData();
        }
    }

    /**
     * 将profile数据写到log中
     *
     * @return
     */
    private void dumpProfileData() {
        ThreadData[] threadDatas = Profiler.threadDatas;
        for (int index = 0; index < threadDatas.length; index++) {
            ThreadData threadData = threadDatas[index];
            if (threadData == null) {
                continue;
            }
            ProfStack<long[]> profile = threadData.profileData;
            while (profile.size() > 0) {
                long[] data = profile.pop();
                StringBuilder sb = new StringBuilder();
                sb.append(index).append('\t');  // thread id
                sb.append(data[1]).append('\t');  // stack number
                sb.append(data[0]).append('\t'); // method id
                sb.append(data[2]).append('\n'); // use time
                fileWriter.append(sb.toString());
            }
            fileWriter.flushAppend();
            threadData.clear();
        }
        fileWriter.append("=\n");
        fileWriter.flushAppend();
    }

    /**
     * 记录Mysql方法的日志
     */
    private void dumpMysqlData() {

        SlowQueryData[] threadData = Profiler.slowQueryProfile;
        for (int index = 0; index < threadData.length; index++) {
            SlowQueryData profilerData = threadData[index];
            if (profilerData == null) {
                continue;
            }
            ProfStack<RecordSlowQuery> profile = profilerData.profileData;
            while (profile.size() > 0) {
                RecordSlowQuery cur = profile.pop();
                StringBuilder sb = new StringBuilder();
                sb.append(cur.getRequestDesc().get("host"));
                sb.append('\t');
                sb.append(cur.getRequestDesc().get("port"));
                sb.append('\t');
                sb.append(cur.getRequestDesc().get("db"));
                sb.append('\t');
                sb.append(cur.getRequestDesc().get("sql"));
                sb.append('\t');
                sb.append(cur.getRequestDesc().get("nanoTime"));
                sb.append('\n');
                mysqlFileWriter.append(sb.toString());
                sb.setLength(0);
            }
            mysqlFileWriter.flushAppend();
            profilerData.clear();
        }
        mysqlFileWriter.append("=\n");
        mysqlFileWriter.flushAppend();

    }
}
