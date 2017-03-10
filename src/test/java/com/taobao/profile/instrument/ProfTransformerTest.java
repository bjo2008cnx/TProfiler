package com.taobao.profile.instrument;

import com.taobao.profile.Manager;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileInputStream;

@Ignore
public class ProfTransformerTest {

    @Test
    public void testTransform() throws Exception {
        Manager.instance().initialization();
        byte[] bytes = StreamUtil.stream2Byte(new FileInputStream("d:/Comparable.class"));
        System.out.println(bytes.length);
        for (byte b : bytes) {
            System.out.print(b);
        }
        byte[] transformed = new ProfTransformer().transform(null, "com.asm3.Comparable", null, null, bytes);
        System.out.println();
        for (byte b : transformed) {
            System.out.print(b);
        }
        try {
            Assert.assertArrayEquals(bytes, transformed);
            Assert.fail("should not be equal.");
        }catch (Exception e){
        }
    }
}