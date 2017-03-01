package com.taobao.profile.runtime;

import com.taobao.profile.Manager;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class MethodCacheTest {

    @Test
    public void testRequest() throws Exception {

    }

    @Test
    public void testUpdateLineNum() throws Exception {

    }

    @Test
    public void testUpdateMethodName() throws Exception {

    }

    @Test
    public void testFlushMethodData() throws Exception {
        Manager.instance().initialization();
        MethodCache.flushMethodData();
        File file = new File(System.getProperty("user.home"), "/logs/tmethod.log" );
        Assert.assertNotNull(file);
        Assert.assertTrue(file.exists());
    }
}