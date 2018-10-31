package com.taobao.profile.thread;

import com.taobao.profile.Manager;
import com.taobao.profile.analysis.ProfilerLogAnalysis;
import com.taobao.profile.analysis.ProfilerLogStackAnalysis;
import com.taobao.profile.report.AnalysisReportManager;
import com.taobao.profile.runtime.MethodCache;

import java.io.OutputStream;

/**
 * @author Michael.Wang
 * @date 2017/3/20
 */
public class CommandHandler {
    public static void start(String testId) {
        Manager.instance().setSwitchFlag(true);
        //TODO 创建文件夹等
    }

    public static void stop(String testId) {
        //Manager.instance().setSwitchFlag(false);
        DataDumper.getInstance().dump();
        flushMethod(testId); //TimeControl线程未启动，需要显示调用flushMethod方法
        ProfilerLogAnalysis.analyzeLog(testId);
        ProfilerLogStackAnalysis.analyzeLog(testId);
    }

    public static void flushMethod(String testId) {
        MethodCache.flushMethodData();
    }

    public static void fetchStatus(String testId, OutputStream out) {
        //TODO 获取状态
    }

    public static void fetchReport(String testId, OutputStream out) {
        AnalysisReportManager.fetchReport(testId,out);
    }
}
