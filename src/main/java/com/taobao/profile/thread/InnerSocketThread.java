/**
 * (C) 2011-2012 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 *
 */
package com.taobao.profile.thread;

import com.taobao.profile.Manager;
import com.taobao.profile.ProfilerConstant;
import com.taobao.profile.runtime.MethodCache;
import lombok.extern.slf4j.Slf4j;
import org.lightfw.util.io.common.StreamUtil;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 对外提供Socket开关
 */
@Slf4j
public class InnerSocketThread extends Thread {
    /**
     * server
     */
    private ServerSocket socket;

    public void run() {
        try {
            socket = new ServerSocket(Manager.PORT);
            while (true) {
                handleCommand();
            }
        } catch (IOException e) {
            log.error("io error", e);
        } finally {
            StreamUtil.close(socket);
        }
    }

    /**
     * 处理客户端发送过来的命令
     *
     * @throws IOException
     */
    private void handleCommand() throws IOException {
        Socket childSocket = socket.accept();
        childSocket.setSoTimeout(5000);
        String commands = StreamUtil.streamToString(childSocket.getInputStream());
        String[] commandStrs = commands.split(":");
        String command = commandStrs[0];
        if (ProfilerConstant.START.equals(command)) {
            Manager.instance().setSwitchFlag(true);
        } else if (ProfilerConstant.STATUS.equals(command)) {
            write(childSocket.getOutputStream());
        } else if (ProfilerConstant.FLUSHMETHOD.equals(command)) {
            MethodCache.flushMethodData();
        } else {
            Manager.instance().setSwitchFlag(false);
        }
        StreamUtil.close(childSocket);
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

    /**
     * 调试使用
     *
     * @param args
     */
    public static void main(String[] args) {
        InnerSocketThread socketThread = new InnerSocketThread();
        socketThread.setName("TProfiler-InnerSocket-Debug");
        socketThread.start();
    }
}
