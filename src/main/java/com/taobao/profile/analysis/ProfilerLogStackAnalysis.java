package com.taobao.profile.analysis;

import com.taobao.profile.config.ProfConfig;
import com.taobao.profile.runtime.MethodTime;
import com.taobao.profile.runtime.MethodTimeHelper;
import com.taobao.profile.utils.StreamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分析Profiler生成的Log, 根据profiler.log和MethodCache,输出top Method及堆栈
 *
 * @since 2017-3-17
 */
public class ProfilerLogStackAnalysis {
    private static Logger log = LoggerFactory.getLogger(ProfilerLogStackAnalysis.class);
    public static final String FILE_TOP_METHOD_STACK_LOG = "/topmethodstack.log";

    private long currentThreadId = -1;
    private List<MethodTime> methodTimes = new ArrayList<>();

    public static void analyzeLog(String testId) {
        //TODO 路径中增加testId
        log.info("Analyze log [start]. testId: " + testId);
        ProfConfig profConfig = ProfConfig.getInstance();
        ProfilerLogStackAnalysis analysis = new ProfilerLogStackAnalysis();
        analysis.readProfilerLog(profConfig.getLogFilePath()); //TODO 边加载边合并
        log.debug("loaded tprofilier.log size is :" + analysis.methodTimes.size());
        List<MethodTime> topLevelMethodTimes = analysis.handleTopMethodTimeStack(analysis.methodTimes);

        Map<Long, MethodTime> mergedTopList = mergetTop(topLevelMethodTimes);
        List<MethodTime> list = new ArrayList();
        list.addAll(mergedTopList.values());
        Map<Long, String> methodLog = MethodLogReader.read(profConfig.getMethodFilePath()); //读取tmethod.log
        MethodStackLogWriter.output(list, profConfig.getLogPath() + FILE_TOP_METHOD_STACK_LOG, methodLog);
        log.info("Analyze log [end]. testId: " + testId);
    }

    /**
     * 合并顶层对象（及children）时间
     *
     * @param topLevelMethodTimes
     * @return
     */
    private static Map<Long, MethodTime> mergetTop(List<MethodTime> topLevelMethodTimes) {
        Map<Long, MethodTime> mergedMethodTimes = new HashMap<>();
        for (MethodTime topMethodTime : topLevelMethodTimes) {
            MethodTime foundMethodTime = mergedMethodTimes.get(topMethodTime.getMethodId());
            if (foundMethodTime != null) {
                MethodTimeHelper.sumDetailTime(foundMethodTime, topMethodTime);
            } else {
                mergedMethodTimes.put(topMethodTime.getMethodId(), topMethodTime);
            }
        }
        return mergedMethodTimes;
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
    private void readProfilerLog(String tprofilerLogPath) {
        BufferedReader reader = null;
        try {
            String line;
            reader = new BufferedReader(new FileReader(tprofilerLogPath));
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("##")) {
                    continue;
                }
                if ("=".equals(line)) {
                    currentThreadId = -1;
                    //merge(this.methodTimes);
                }
                String[] data = line.split("\t");
                if (data.length != 4) {
                    continue;
                }
                long threadId = Long.parseLong(data[0]);
                if (currentThreadId != threadId) {
                    currentThreadId = threadId;
                    //merge(this.methodTimes);
                }
                MethodTime methodTime = buildMethodTime(data);
                this.methodTimes.add(methodTime);
            }
        } catch (FileNotFoundException e) {
            log.error("Analyze error", e);
        } catch (IOException e) {
            log.error("Analyze error", e);
        } finally {
            StreamUtil.close(reader);
        }
        //merge(this.methodTimes);
    }

    /**
     * 构造MethodTime对象
     *
     * @param data
     * @return
     */
    private MethodTime buildMethodTime(String[] data) {
        MethodTime methodTime = new MethodTime();
        methodTime.setStackNum(Long.parseLong(data[1]));
        methodTime.setMethodId(Long.parseLong(data[2]));
        methodTime.setUseTime(Long.parseLong(data[3]));
        methodTime.setRawTime(methodTime.getUseTime());//初始化时取相同值
        methodTime.setExecuteTimes(1);
        return methodTime;
    }

    /**
     * 处理堆栈
     */
    private List<MethodTime> handleTopMethodTimeStack(List<MethodTime> methodTimeList) {
        log.debug("handleTopMethodTimeStack [start] ");
        List<MethodTime> topLevelMethodTimes = new ArrayList<>();
        for (int i = 0; i < methodTimeList.size(); i++) {
            MethodTime currentNode = methodTimeList.get(i);
            if (currentNode.getStackNum() == 0) {
                topLevelMethodTimes.add(currentNode);
                handleNextNode(currentNode, methodTimeList, i);
            }
        }
        log.debug("handleTopMethodTimeStack [end]. top level method time size is: " + topLevelMethodTimes.size());
        return topLevelMethodTimes;
    }

    /**
     * 处理下一个级点，并组织节点间关系
     *
     * @param currentNode
     * @param methodTimeList
     * @param i
     */
    private void handleNextNode(MethodTime currentNode, List<MethodTime> methodTimeList, int i) {
        if (i == methodTimeList.size() - 1) { //超出界外
            return;
        }
        MethodTime nextNode = methodTimeList.get(i + 1);
        if (nextNode.getStackNum() == 0) {
            return;
        }
        if (currentNode.getStackNum() == nextNode.getStackNum() - 1) {
            nextNode.setParent(currentNode);
            currentNode.addChild(nextNode);  //下一级
        } else if (currentNode.getStackNum() == nextNode.getStackNum()) {
            nextNode.setParent(currentNode.getParent());
            currentNode.getParent().addChild(nextNode);   //同级
        } else {
            nextNode.setParent(currentNode.getParent().getParent());
            currentNode.getParent().getParent().addChild(nextNode); //更高级
        }
        handleNextNode(nextNode, methodTimeList, i + 1);
    }
}
