package com.taobao.profile.thread;

import org.junit.Test;

public class InnerSocketThreadTest {

    @Test
    public void testRun() throws Exception {
        InnerSocketThread t =  new InnerSocketThread();
        t.start();
    }
}