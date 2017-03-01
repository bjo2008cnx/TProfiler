package com.taobao.profile.thread;

import com.taobao.profile.config.ProfConfig;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class DataDumpThreadTest {

    @Test
    public void testRun() throws Exception {
        ProfConfig config = new ProfConfig();
        config.setDebugMode(true);
        Thread t = new DataDumpThread(config);
        t.start();
        Thread.sleep(2000);
        File file = new File(System.getProperty("user.home"), "/logs/tprofiler.log" );
        Assert.assertNotNull(file);
        Assert.assertTrue(file.exists());
    }
}