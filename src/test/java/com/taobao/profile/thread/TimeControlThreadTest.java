package com.taobao.profile.thread;

import com.taobao.profile.Manager;
import com.taobao.profile.config.ProfConfig;
import mockit.NonStrictExpectations;
import org.junit.Test;

import java.util.Calendar;

public class TimeControlThreadTest {
    public static void main(String[] args) throws Exception {
        new TimeControlThreadTest(). testRun();
    }

    @Test
    public void testRun() throws Exception {
        final Calendar cal = Calendar.getInstance();
        cal.set(2017, Calendar.MARCH, 1, 10, 55, 03); //2017/3/1 10:55:03

        new NonStrictExpectations(){{
            System.currentTimeMillis(); //指定要mock的方法
            result =  cal.getTime().getTime(); //指定mock方法要返回的结果
            times=0;
        }};

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