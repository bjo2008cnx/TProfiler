package com.taobao.profile.analysis;

import com.taobao.profile.Manager;
import org.junit.Assert;
import org.junit.Test;

public class ProfilerLogStackAnalysisTest {
    @Test
    public void testAnalyzeLog() throws Exception {
        try {
            Manager.instance().initialization();
            ProfilerLogStackAnalysis.analyzeLog("1");
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}