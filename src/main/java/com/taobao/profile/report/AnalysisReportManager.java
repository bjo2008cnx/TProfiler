package com.taobao.profile.report;

import com.taobao.profile.config.ProfConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @descirption
 * @Author meng.wang
 * @Date 2017-03-23
 **/
public class AnalysisReportManager {

    private static Logger log = LoggerFactory.getLogger(AnalysisReportManager.class);

    public static void fetchReport(String testId,OutputStream out){
        try {
            //TODO 处理 分析日志上传 增加testId
            File logsDir = new File(ProfConfig.getInstance().getLogPath());
            if(logsDir.exists() && logsDir.isDirectory()){
                File[] files = logsDir.listFiles();
                File logFile ;
                if(files.length >0 ){
                    DataOutputStream ops = new DataOutputStream(out);
                    ops.writeInt(files.length);
                    ops.flush();
                    for(int i = 0;i<files.length;i++){
                        try {
                            logFile = files[i];
                            log.info("fetchReport file:{},",logFile.getName());
                            DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(logFile)));
                            ops.writeUTF(logFile.getName());
                            ops.flush();
                            ops.writeLong(logFile.length());
                            ops.flush();
                            int bufferSize = 1024;
                            byte[] buf = new byte[bufferSize];
                            while (true) {
                                int read = 0;
                                if (fis != null) {
                                    read = fis.read(buf);
                                }
                                if (read == -1) {
                                    break;
                                }
                                ops.write(buf, 0, read);
                            }
                            ops.flush();
                            fis.close();
                        } catch (IOException e) {
                            log.error("fail to handle file.",e);//捕捉异常以避免部分文件或文件夹无权限操作引起全部文件无法处理
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("fail to retch report.", e);
        }
    }

}
