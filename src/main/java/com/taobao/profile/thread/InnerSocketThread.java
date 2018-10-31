package com.taobao.profile.thread;

import com.taobao.profile.Manager;
import com.taobao.profile.client.ProfilerConstant;
import com.taobao.profile.config.ProfConfig;
import com.taobao.profile.utils.StreamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 对外提供Socket开关.扩展InnerSocketThread类，Start与Stop功能与原有功能有以下不同：<br/>
 * Start：只有在接收到start命令后才启动数据抓紧，而不是在tomcat启动时自动启动<br/>
 * Stop：stop profiler时，自动进行分析，输出top methods&top objects<br/>
 *
 * @since 2017-3-17
 */
public class InnerSocketThread extends Thread {
    private static Logger log = LoggerFactory.getLogger(InnerSocketThread.class);
    /**
     * server
     */
    private ServerSocket socket;

    public void run() {
        try {
            socket = new ServerSocket(ProfConfig.getInstance().getPort());
            while (true) {
                try {
                    Socket child = socket.accept();
                    child.setSoTimeout(5000);

                    String commandJson = StreamUtil.streamToString(child.getInputStream());
                    handleCommand(commandJson, child.getOutputStream());
                    child.close();
                } catch (Throwable t) {
                    log.error("fail to accept socket", t);
                }
            }
        } catch (IOException e) {
            log.error("Error occurred while handling commands", e);
        } finally {
            StreamUtil.close(socket);
        }
    }

    /**
     * 处理客户端发过来的命令
     *
     * @param commandStr
     * @param out
     */
    private void handleCommand(String commandStr, OutputStream out) {
        if (commandStr == null) {
            return;
        }
        String[] commands = commandStr.split(",");
        if (commands.length != 2) {
            log.error("Wrong command format.");
            return;
        }

        String command = commands[0];
        String testId = commands[1];

        String info = "received command : %s ,test_id : %s from client.";
        log.info(String.format(info,command,testId));
        if (ProfilerConstant.START.equals(command)) {
            CommandHandler.start(testId);
        } else if (ProfilerConstant.FLUSHMETHOD.equals(command)) {
            CommandHandler.flushMethod(testId);
        } else if (ProfilerConstant.STOP.equals(command)) {
            CommandHandler.stop(testId);
        } else if (ProfilerConstant.STATUS.equals(command)) {
            CommandHandler.fetchStatus(testId, out);
        } else if (ProfilerConstant.FETCHREPORT.equals(command)){
            log.info("command");
            CommandHandler.fetchReport(testId,out);
        }
    }


    /**
     * TODO 分析性能,将结果写到另一张表中
     *
     * @param testId
     */
    private void analizeLog(String testId) {
        //throw ProfilerConstant.RE_TODO;
    }

    /**
     * 输出状态
     *
     * @param os
     * @throws IOException
     */
    private void write(OutputStream os) throws IOException {
        BufferedOutputStream out = new BufferedOutputStream(os);
        if (Manager.instance().getSwitchFlag()) {
            out.write("running".getBytes());
        } else {
            out.write("stop".getBytes());
        }
        out.write('\r');
        out.flush();
    }

}
