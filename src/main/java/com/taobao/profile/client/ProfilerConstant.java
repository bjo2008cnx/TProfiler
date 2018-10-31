package com.taobao.profile.client;

/**
 * @author Michael.Wang
 * @date 2017/3/17
 */
public interface ProfilerConstant {
    /**
     * 开始命令
     */
    public String START = "start";

    /**
     * 结束命令
     */
    public String STOP = "stop";

    /**
     * 获取状态命令
     */
    public String STATUS = "status";

    /**
     * 远程刷出方法数据
     */
    public String FLUSHMETHOD = "flushmethod";

    /**
     * 下载远程服务器日志
     */
    public String FETCHREPORT = "fetchReport";

    /**
     * 命令
     */
    public String COMMAND = "command";

    /**
     * test_id 参数
     */
    public String TEST_ID = "test_id";

    public static final RuntimeException RE_TODO = new RuntimeException("TODO");
}
