package com.taobao.profile.thread;

import com.taobao.profile.config.ProfConfig;
import org.junit.Test;

public class TimeControlThreadTest {

    @Test
    public void testRun() throws Exception {
        ProfConfig config = new ProfConfig();
        config.setStartProfTime("09:00:00");
        config.setStartProfTime("23:00:00");
        config.setDebugMode(true);
        Thread t = new TimeControlThread(config);
        t.start();
    }

    @Test
    public void testParse() throws Exception {
    }

    @Test
    public void testWaitTime() throws Exception {

    }

    @Test
    public void testNextStartTime() throws Exception {

    }

    @Test
    public void testConstructor() throws Exception {

    }
}