package com.taobao.profile.analysis;

import com.taobao.profile.runtime.MethodTime;
import com.taobao.profile.utils.MathUtils;
import com.taobao.profile.utils.StreamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Michael.Wang
 * @date 2017/4/6
 */
public class MethodStackLogWriter {

    private static Logger log = LoggerFactory.getLogger(MethodStackLogWriter.class);

    /**
     * 输出分析结果
     */
    public static void output(List<MethodTime> list, String topMethodPath, Map<Long, String> methodLog) {
        Collections.sort(list, MethodTime.USE_TIME_COMPARATOR);

        BufferedWriter topMethodWriter = null;
        try {
            topMethodWriter = new BufferedWriter(new FileWriter(topMethodPath));
            for (MethodTime methodTime : list) {
                outputMethodTime(topMethodWriter, methodTime,methodLog);
            }
            topMethodWriter.flush();
        } catch (IOException e) {
            log.error("Analyze error", e);
        } finally {
            StreamUtil.close(topMethodWriter);
        }
    }

    private static void outputMethodTime(BufferedWriter topMethodWriter, MethodTime methodTime, Map<Long, String> methodLog) throws IOException {
        StringBuilder sb = new StringBuilder();
        long executeNum = methodTime.getExecuteTimes();
        long allTime;
        allTime = methodTime.getRawTime();
        long useTime = MathUtils.div(allTime, executeNum);
        sb.append(methodLog.get(methodTime.getMethodId())).append("\t");
        sb.append(methodTime.getStackNum()).append("\t");
        sb.append(executeNum).append("\t");
        sb.append(useTime).append("\t");
        sb.append(allTime).append("\n");
        topMethodWriter.write(sb.toString());

        //output children
        List<MethodTime> children = methodTime.getChildren();
        if (children != null) {
            for (MethodTime childMethodTime : children) {
                outputMethodTime(topMethodWriter, childMethodTime, methodLog);
            }
        }
    }
}
