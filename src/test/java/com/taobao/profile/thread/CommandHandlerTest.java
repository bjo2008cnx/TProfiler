package com.taobao.profile.thread;

import com.taobao.profile.Manager;
import org.junit.Test;

public class CommandHandlerTest {

    @Test
    public void testStart() throws Exception {

    }

    @Test
    public void testStop() throws Exception {
        Manager.instance().initialization();
        CommandHandler.stop("892");
    }

    @Test
    public void testFlushMethod() throws Exception {

    }

    @Test
    public void testFetchStatus() throws Exception {

    }

    @Test
    public void testFetchReport() throws Exception {

    }
}