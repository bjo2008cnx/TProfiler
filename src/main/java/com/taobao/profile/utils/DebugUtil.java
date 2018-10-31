package com.taobao.profile.utils;

import com.taobao.profile.Profiler;
import com.taobao.profile.dependence_query.SlowQueryData;
import com.taobao.profile.runtime.ThreadData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael.Wang
 * @date 2017/3/16
 */
public class DebugUtil {
    private static Logger log = LoggerFactory.getLogger(DebugUtil.class);

    public static void printThreadStack() {
        ThreadData[] threadDataArray = Profiler.threadDataArray;
        if (threadDataArray == null) {
            log.debug("threadDataArray is null");
            return;
        }
        for (ThreadData threadData : threadDataArray) {
            if (threadData == null){
                log.debug("threadData is null");
                break;
            }
            if (threadData.profileData != null) {
                log.debug("stack size is: " + threadData.profileData.size());
            } else {
                log.debug("stack size is 0");
            }
        }
    }

    public static void printSlowQueryStack() {
        SlowQueryData[] slowQueryDataArray = Profiler.slowQueryDataArray;
        if (slowQueryDataArray == null) {
            log.debug("slowQueryDataArray is null");
            return;
        }
        for (SlowQueryData slowQueryData : slowQueryDataArray) {
            if (slowQueryData == null){
                log.debug("slowQueryData is null");
                break;
            }
            if (slowQueryData.profileData != null) {
                log.debug("stack size is: " + slowQueryData.profileData.size());
            } else {
                log.debug("stack size is 0");
            }
        }
    }
}
