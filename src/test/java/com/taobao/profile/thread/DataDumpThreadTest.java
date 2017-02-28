package com.taobao.profile.thread;

import com.taobao.profile.config.ProfConfig;
import org.junit.Test;

public class DataDumpThreadTest {

    @Test
    public void testRun() throws Exception {
        ProfConfig config = new ProfConfig();
        config.setDebugMode(true);
        Thread t = new DataDumpThread(config);
        t.start();
    }
}