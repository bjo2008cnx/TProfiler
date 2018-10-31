package com.taobao.profile.analysis;

import com.taobao.profile.utils.StreamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael.Wang
 *
 * @date 2017/4/6
 */
public class MethodLogReader {
    private static Logger log = LoggerFactory.getLogger(MethodLogReader.class);
    /**
     * 读取tmethod.log
     *
     * @param methodPath
     * @throws IOException
     */
    public static Map<Long, String> read(String methodPath) {
        Map<Long, String> methodIdMap = new HashMap<>();
        BufferedReader methodLogReader = null;
        try {
            methodLogReader = new BufferedReader(new FileReader(methodPath));
            String line;
            while ((line = methodLogReader.readLine()) != null) {
                if (line.startsWith("instrument")) {
                    continue;
                }
                String[] data = line.split(" ");
                if (data.length != 2) {
                    continue;
                }
                methodIdMap.put(Long.parseLong(data[0]), String.valueOf(data[1]));
            }
        } catch (IOException e) {
            log.error("fail to read tmethod.log", e);
        } finally {
            StreamUtil.close(methodLogReader);
        }
        return methodIdMap;
    }
}
