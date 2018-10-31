package com.taobao.profile.analysis;

import com.taobao.profile.config.ProfConfig;
import com.taobao.profile.runtime.MethodTime;
import com.taobao.profile.utils.MathUtils;
import com.taobao.profile.utils.StreamUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * 分析Profiler生成的Log, 根据profiler.log和MethodCache,输出top Method及堆栈
 *
 * @since 2017-3-17
 */
public class ProfilerLogAnalysis {
    private static Logger log = LoggerFactory.getLogger(ProfilerLogAnalysis.class);
    public static final String TOP_METHOD_LOG = "/topmethod.log";

    private boolean nano = false;
    private long currentThreadId = -1;
    private List<MethodTime> methodTimes = new ArrayList<>();
    private Map<Long, MethodTime> cachedMethodMap = new HashMap<>();
    private Map<Long, String> methodIdMap = new HashMap<>();

    public static void analyzeLog(String testId) {
        log.info("Start to analyze log for test ", testId);
        // 路径中增加testId
        ProfConfig profConfig = ProfConfig.getInstance();
        ProfilerLogAnalysis analysis = new ProfilerLogAnalysis();
        analysis.read(profConfig.getLogFilePath(), profConfig.getMethodFilePath());
        analysis.output(profConfig.getLogPath() + TOP_METHOD_LOG);
        log.info("Analyzing log  ends. testId is :." + testId);
    }

    public static void analyze(final String testId) {
        Runnable t = new Runnable() {
            @Override
            public void run() {
                analyzeLog(testId);
            }
        };
        new Thread(t).start();
    }

    /**
     * 读取log,并解析
     */
    private void read(String tprofilerLogPath, String tmethodLogPath) {
        BufferedReader reader = null;
        try {
            readMethodLog(tmethodLogPath);
            String line;
            log.info("tprofiler.log location is:" + tprofilerLogPath);
            reader = new BufferedReader(new FileReader(tprofilerLogPath));
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("##")) {
                    line = line.substring(line.indexOf(":") + 1, line.length());
                    nano = line.equals("true");
                    continue;
                }
                if ("=".equals(line)) {
                    currentThreadId = -1;
                    merge(this.methodTimes);
                }
                String[] data = line.split("\t");
                if (data.length != 4) {
                    continue;
                }
                long threadId = Long.parseLong(data[0]);
                if (currentThreadId != threadId) {
                    currentThreadId = threadId;
                    merge(this.methodTimes);
                }
                MethodTime methodTime = new MethodTime();
                methodTime.setStackNum(Long.parseLong(data[1]));
                methodTime.setMethodId(Long.parseLong(data[2]));
                methodTime.setUseTime(Long.parseLong(data[3]));
                methodTime.setExecuteTimes(1);
                methodTimes.add(methodTime);
            }
        } catch (FileNotFoundException e) {
            log.error("Analyze error", e);
        } catch (IOException e) {
            log.error("Analyze error", e);
        } finally {
            StreamUtil.close(reader);
            log.info("read tprofiler.log [end]. size is:" + methodTimes.size());
        }
        merge(this.methodTimes);
    }

    /**
     * 读取tmethod.log
     *
     * @param methodPath
     * @throws IOException
     */
    private void readMethodLog(String methodPath) {
        log.info("read method log [start].  methodPath :" + methodPath);
        BufferedReader methodLogReader = null;
        try {
            methodLogReader = new BufferedReader(new FileReader(methodPath));
            String line;
            while ((line = methodLogReader.readLine()) != null) {
                if (line.startsWith("instrument")) {
                    continue;
                }
                String[] data = line.split(" ");
                if (data.length != 2|| StringUtils.isEmpty(data[0]) ||StringUtils.isEmpty(data[1])) {
                    continue;
                }
                methodIdMap.put(Long.parseLong(data[0]), String.valueOf(data[1]));
            }
        } catch (IOException e) {
            log.error("fail to read tmethod.log", e);
        } finally {
            StreamUtil.close(methodLogReader);
            log.debug("methodIdMap size is:" + methodIdMap.size());
            log.info("read method log  [end]" + methodPath);
        }
    }

    /**
     * 合并数据
     */
    private void merge(List<MethodTime> methodTimeList) {
        handleNetTime(methodTimeList);
        sumMethodTime(methodTimeList);
        methodTimeList.clear();
    }

    /**
     * 计算方法总耗时
     *
     * @param methodTimeList
     */
    private void sumMethodTime(List<MethodTime> methodTimeList) {
        for (int i = 0; i < methodTimeList.size(); i++) {
            MethodTime m = methodTimeList.get(i);
            long methodId = m.getMethodId();
            MethodTime methodTime = cachedMethodMap.get(methodId);
            if (methodTime == null) {
                methodTime = new MethodTime();
                methodTime.setMethodId(methodId);
                methodTime.setMethodName(methodIdMap.get(methodId));
                methodTime.setExecuteTimes(1);
                methodTime.sum(m);
                cachedMethodMap.put(methodId, methodTime);
            } else {
                methodTime.sum(m); //如果已有，则进行汇总
            }
        }
    }

    /**
     * 处理净耗时
     */
    private void handleNetTime(List<MethodTime> methodTimeList) {
        for (int i = 0; i < methodTimeList.size(); i++) {
            MethodTime m = methodTimeList.get(i);
            long stackNum = m.getStackNum();
            for (int j = i + 1; j < methodTimeList.size(); j++) {
                MethodTime nextMethodTime = methodTimeList.get(j);
                long tmpStack = nextMethodTime.getStackNum();
                if (stackNum + 1 == tmpStack) {
                    m.setUseTime(m.getUseTime() - nextMethodTime.getUseTime()); //净耗时
                    if (m.getChildren() == null) {
                        m.setChildren(new ArrayList<MethodTime>());
                    }
                    m.getChildren().add(nextMethodTime); //加入调用链
                } else if (stackNum >= tmpStack) {
                    break;
                }
            }
        }
    }

    /**
     * 输出分析结果
     */
    public void output(String topMethodPath) {
        List<MethodTime> list = new ArrayList<>();
        list.addAll(cachedMethodMap.values());

        Collections.sort(list, MethodTime.USE_TIME_COMPARATOR);

        BufferedWriter topMethodWriter = null;
        try {
            topMethodWriter = new BufferedWriter(new FileWriter(topMethodPath));
            for (MethodTime methodTime : list) {
                StringBuilder sb = new StringBuilder();
                long executeNum = methodTime.getExecuteTimes();
                long allTime;
                allTime = nano ? MathUtils.div(methodTime.getUseTime(), 1000000) : methodTime.getUseTime();
                long useTime = MathUtils.div(allTime, executeNum);
                sb.append(methodTime.getMethodName()).append("\t");
                sb.append(executeNum).append("\t");
                sb.append(useTime).append("\t");
                sb.append(allTime).append("\n");
                topMethodWriter.write(sb.toString());
            }
            topMethodWriter.flush();
        } catch (IOException e) {
            log.error("Analyze error", e);
        } finally {
            StreamUtil.close(topMethodWriter);
        }
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: <tprofiler.log path> <tmethod.log path> <topmethod.log path> ");
            return;
        }
        ProfilerLogAnalysis analysis = new ProfilerLogAnalysis();
        analysis.read(args[0], args[1]);
        analysis.output(args[2]);
    }
}
