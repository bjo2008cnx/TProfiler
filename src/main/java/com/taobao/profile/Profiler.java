/**
 * (C) 2011-2012 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 *
 */
package com.taobao.profile;

import com.taobao.profile.dependence_query.RecordSlowQuery;
import com.taobao.profile.dependence_query.SlowQueryData;
import com.taobao.profile.runtime.ThreadData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 此类收集应用代码的运行时数据
 *
 * @author luqi
 * @since 2010-6-23
 */
public class Profiler {
    private static Logger log = LoggerFactory.getLogger(Profiler.class);
    /**
     * 注入类数
     */
    public static AtomicInteger instrumentClassCount = new AtomicInteger(0);
    /**
     * 注入方法数
     */
    public static AtomicInteger instrumentMethodCount = new AtomicInteger(0);

    private final static int size = 65535;
    /**
     * 线程数组
     */
    public static ThreadData[] threadDataArray = new ThreadData[size];

    /**
     * 记录慢日志的数组
     */
    public static SlowQueryData[] slowQueryDataArray = new SlowQueryData[size];

    /**
     * 方法开始时调用,采集开始时间
     *
     * @param methodId
     */
    public static void Start(int methodId) {
        //log.debug("call 'Start'");
        if (!Manager.instance().canProfile()) {
            //log.warn("Manager.instance().canProfile() is false.Can't start profiling.");
            return;
        }
        long threadId = Thread.currentThread().getId();
        if (threadId >= size) {
            log.warn("threadId >= size is true ");
            return;
        }

        long startTime = Manager.isNeedNanoTime() ? System.nanoTime() : System.currentTimeMillis();

        try {
            //采集线程数据
            ThreadData threadData = threadDataArray[(int) threadId];
            if (threadData == null) {
                threadData = new ThreadData();
                threadDataArray[(int) threadId] = threadData;
            }

            long[] frameData = new long[3];
            frameData[0] = methodId;
            frameData[1] = threadData.stackNum;
            frameData[2] = startTime;
            threadData.stackFrame.push(frameData);
            threadData.stackNum++;
        } catch (Exception e) {
            log.error("fail to call Start.",e);
        }
    }

    /**
     * 方法退出时调用,采集结束时间
     *
     * @param methodId
     */
    public static void End(int methodId) {
        //log.debug("call 'End'");
        if (!Manager.instance().canProfile()) {
            //log.warn("canProfile is false");
            return;
        }
        long threadId = Thread.currentThread().getId();
        if (threadId >= size) {
            log.warn("threadId is bigger thean buffer size.");
            return;
        }

        long endTime = Manager.isNeedNanoTime() ? System.nanoTime() : System.currentTimeMillis();
        try {
            ThreadData thrData = threadDataArray[(int) threadId];
            if (thrData == null || thrData.stackNum <= 0 || thrData.stackFrame.size() == 0) {
                // 没有执行start,直接执行end/可能是异步停止导致的
                return;
            }
            // 栈太深则抛弃部分数据
            if (thrData.profileData.size() > 20000) {
                thrData.stackNum--;
                thrData.stackFrame.pop();
                return;
            }
            thrData.stackNum--;
            long[] frameData = thrData.stackFrame.pop();
            long id = frameData[0];
            if (methodId != id) {
                return;
            }
            long useTime = endTime - frameData[2];
            if (Manager.isNeedNanoTime()) {
                if (useTime > 500000) {
                    frameData[2] = useTime;
                    thrData.profileData.push(frameData);
                }
            } else if (useTime > 1) {
                frameData[2] = useTime;
                thrData.profileData.push(frameData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clearData() {
        for (int index = 0; index < threadDataArray.length; index++) {
            ThreadData profilerData = threadDataArray[index];
            if (profilerData == null) {
                continue;
            }
            profilerData.clear();
        }

        for (int index = 0; index < slowQueryDataArray.length; index++) {
            SlowQueryData profilerData = slowQueryDataArray[index];
            if (profilerData == null) {
                continue;
            }
            profilerData.clear();
        }
    }

    /** add for dependence**/

    /**
     * 获取线程ID；如果不在需要profile的区间内；则返回-1
     *
     * @return
     */
    private static long getThreadID() {
        if (!Manager.instance().canProfile()) {
            return -1;
        }
        long threadId = Thread.currentThread().getId();
        if (threadId >= size) {
            return -1;
        }
        return threadId;
    }

    /**
     * 获取时间；用于计算函数执行时间；支持纳秒取值；
     *
     * @return
     */
    private static long getCurTime() {
        long curTime;
        if (Manager.isNeedNanoTime()) {
            curTime = System.nanoTime();
        } else {
            curTime = System.currentTimeMillis();
        }
        return curTime;
    }

    /**
     * 获取当前线程的信息;如果不存在则会重新分配一个；
     *
     * @param threadId
     * @return
     */
    private static SlowQueryData getThreadData(long threadId) {
        SlowQueryData thrData = slowQueryDataArray[(int) threadId];
        if (thrData == null) {
            thrData = new SlowQueryData();
            slowQueryDataArray[(int) threadId] = thrData;
        }
        return thrData;
    }

    /**
     * 是否需要记录;只超过10ms的查询
     *
     * @param useTime
     * @return
     */
    private static boolean isNeedRecord(long useTime) {
        int time = Manager.getRecordTime();
        if (Manager.isNeedNanoTime()) {
            time = time * 1000000;
            if (useTime > time) {
                return true;
            }
        } else if (useTime > time) {
            return true;
        }
        return false;
    }

    /**
     * 弹出方法的堆栈信息；
     *
     * @param thrData
     * @return
     */
    private static Object[] popStack(SlowQueryData thrData) {
        if (thrData == null) {
            return null;
        }
        // 栈太深则抛弃部分数据
        if (thrData.profileData.size() > 20000) {
            thrData.stackNum--;
            thrData.stackFrame.pop();
            return null;
        }
        thrData.stackNum--;
        Object[] frameData = thrData.stackFrame.pop();
        return frameData;
    }

    /**
     * 开始记录mysql的信息
     *
     * @param host
     * @param port
     * @param db
     * @param sql
     */
    public static void start4Mysql(String host, int port, String db, String sql) {
        long threadId = getThreadID();

        if (threadId == -1) {
            return;
        }

        if (Manager.getRecordTime() == -1) {
            return;
        }

        long startTime = getCurTime();
        try {
            SlowQueryData thrData = getThreadData(threadId);

            Object[] frameData = new Object[6];
            frameData[0] = thrData.stackNum;
            frameData[1] = startTime;
            frameData[2] = host;
            frameData[3] = port;
            frameData[4] = db;
            frameData[5] = sql;
            thrData.stackFrame.push(frameData);
            thrData.stackNum++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * mysql记录结束
     */
    public static void end4Mysql() {
        long threadId = getThreadID();

        if (threadId == -1) {
            return;
        }

        if (Manager.getRecordTime() == -1) {
            return;
        }

        long endTime = getCurTime();

        SlowQueryData thrData = getThreadData(threadId);
        Object[] frameData = popStack(thrData);
        if (frameData == null) {
            return;
        }

        RecordSlowQuery record = new RecordSlowQuery();
        Map<String, String> map = new HashMap<String, String>();

        map.put("host", (String) frameData[2]);
        map.put("port", frameData[3].toString());
        map.put("db", (String) frameData[4]);
        map.put("sql", (String) frameData[5]);
        record.setRequestDesc(map);
        record.setUseTime(endTime - (Long) frameData[1]);
        record.setType("MYSQL");

        StringBuilder sb = new StringBuilder();
        sb.append("MYSQL");
        sb.append((String) frameData[2]);
        sb.append(frameData[3].toString());
        sb.append((String) frameData[4]);

        if (!isNeedRecord(record.getUseTime())) {
            return;
        }
        map.put("nanoTime", Manager.isNeedNanoTime() + "");

        thrData.profileData.push(record);
    }
}
