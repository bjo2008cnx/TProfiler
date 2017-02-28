package com.taobao.profile.config;

import org.junit.Assert;
import org.junit.Test;

public class ProfConfigTest {

    @Test
    public void testConstructor() throws Exception {
        System.setProperty("tprofiler.debug","true");
        ProfConfig config = new ProfConfig();
        Assert.assertEquals(50000,config.getPort());
    }
}