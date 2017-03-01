package com.taobao.profile.thread;

import com.taobao.profile.Manager;
import com.taobao.profile.config.ProfConfig;
import org.junit.Test;

public class TimeControlThreadTest {
    public static void main(String[] args) throws Exception {
        new TimeControlThreadTest(). testRun();
    }

    @Test
    public void testRun() throws Exception {
        Manager.instance().initialization();
        ProfConfig config = new ProfConfig();
        config.setStartProfTime("09:00:00");
        config.setEndProfTime("11:27:07");
        config.setDebugMode(true);

        TimeControlThread t = new TimeControlThread(config);
        t.run();
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