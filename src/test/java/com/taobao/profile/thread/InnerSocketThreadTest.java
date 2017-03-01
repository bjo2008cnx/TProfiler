package com.taobao.profile.thread;

import org.junit.Test;

public class InnerSocketThreadTest {

    @Test
    public void testRun() throws Exception {
        Thread t =  new InnerSocketThread();
        t.start();
    }
}