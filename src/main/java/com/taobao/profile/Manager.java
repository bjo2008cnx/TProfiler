/**
 * (C) 2011-2012 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 *
 */
package com.taobao.profile;

import com.taobao.profile.config.ProfConfig;
import com.taobao.profile.config.ProfFilter;
import com.taobao.profile.thread.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 管理类,单例实现
 *
 * @author shutong.dy
 * @since 2012-1-9
 */
public class Manager {

    /**
     * Manager
     */
    private static Manager manager = new Manager();

    /**
     * 开关标记.远程开始或结束的开关. default:true 打开状态
     */
    private volatile boolean switchFlag = true;
    /**
     * profile标记.是否可以profile. default:false 不可以profile
     */
    private volatile boolean profileFlag = false;

    /**
     * 启动时间是否大于采集结束时间
     */
    private boolean moreThanEndTime;
    /**
     * 是否进入调试模式
     */
    private boolean isDebugMode;

    /**
     * 记录慢查询的时间；超过这个值的查询才会记录；如果设置为-1表示不启用慢日志记录
     */
    private static int recordTime;

    /**
     * 私有构造器
     */
    private Manager() {
    }

    /**
     * 初始化配置
     */
    public void initialization() {
        ProfConfig profConfig = ProfConfig.getInstance();
        // 判断启动时间是否大于采集结束时间 2012-05-25
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        String now = df.format(new Date());
        //moreThanEndTime = (now.compareTo(profConfig.getEndProfTime()) > 0);//
        moreThanEndTime = false;//默认不结束
        isDebugMode = profConfig.isDebugMode();
        recordTime = profConfig.getRecordTime();
        setProfFilter(profConfig);
    }

    /**
     * @return
     */
    public static Manager instance() {
        return manager;
    }

    /**
     * @return the needNanoTime
     */
    public static boolean isNeedNanoTime() {
        return ProfConfig.getInstance().isNeedNanoTime();
    }

    /**
     * @return the ignoreGetSetMethod
     */
    public static boolean isIgnoreGetSetMethod() {
        return ProfConfig.getInstance().isIgnoreGetSetMethod();
    }

    /**
     * @param value the switchFlag to set
     */
    public void setSwitchFlag(boolean value) {
        switchFlag = value;
    }

    /**
     * @param value the profileFlag to set
     */
    public void setProfileFlag(boolean value) {
        profileFlag = value;
    }

    /**
     * 判断当前是否可以采集数据
     *
     * @return
     */
    public boolean canProfile() {
        return profileFlag;
    }

    /**
     * @return
     */
    public boolean getSwitchFlag() {
        return switchFlag;
    }

    /**
     * 判断当前是否可以dump数据
     *
     * @return
     */
    public boolean canDump() {
        return switchFlag;
    }

    /**
     * 启动时间是否大于profile结束时间
     *
     * @return
     */
    public boolean isMoreThanEndTime() {
        return moreThanEndTime;
    }

    public static int getRecordTime() {
        return recordTime;
    }

    public static void setRecordTime(int recordTime) {
        Manager.recordTime = recordTime;
    }

    /**
     * 设置包名过滤器
     */
    private void setProfFilter(ProfConfig profConfig) {
        String classLoader = profConfig.getExcludeClassLoader();
        if (classLoader != null && classLoader.trim().length() > 0) {
            String[] _classLoader = classLoader.split(";");
            for (String pack : _classLoader) {
                ProfFilter.addExcludeClassLoader(pack);
            }
        }
        String include = profConfig.getIncludePackageStartsWith();
        if (include != null && include.trim().length() > 0) {
            String[] _includes = include.split(";");
            for (String pack : _includes) {
                ProfFilter.addIncludeClass(pack);
            }
        }
        String exclude = profConfig.getExcludePackageStartsWith();
        if (exclude != null && exclude.trim().length() > 0) {
            String[] _excludes = exclude.split(";");
            for (String pack : _excludes) {
                ProfFilter.addExcludeClass(pack);
            }
        }
    }

    /**
     * 启动内部线程
     */
    public void startupThread() {
        TimeControlThread controlThread = new TimeControlThread(ProfConfig.getInstance());
        controlThread.setName("TProfiler-TimeControl");
        controlThread.setDaemon(true);
        controlThread.start();

        InnerSocketThread socketThread = new InnerSocketThread();
        socketThread.setName("TProfiler-InnerSocket");
        socketThread.setDaemon(true);
        socketThread.start();

        DataDumpThread dumpThread = new DataDumpThread();
        dumpThread.setName("TProfiler-DataDump");
        dumpThread.setDaemon(true);
        dumpThread.start();
    }
}
