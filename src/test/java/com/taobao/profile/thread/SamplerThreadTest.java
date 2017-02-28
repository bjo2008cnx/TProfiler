package com.taobao.profile.thread;

import com.taobao.profile.config.ProfConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 测试Sampler文件的生成
 */
public class SamplerThreadTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testRun() throws Exception {
        ProfConfig config = new ProfConfig();
        config.setDebugMode(true);
        SamplerThread t = new SamplerThread(config);
        t.run();
    }
}