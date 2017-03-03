package com.taobao.profile.instrument;

import org.junit.Test;

import java.io.FileInputStream;

public class ProfTransformerTest {

    @Test
    public void testTransform() throws Exception {

        byte[] bytes = StreamUtil.stream2Byte(new FileInputStream("d:/Comparable.class"));
        System.out.println(bytes.length);
        byte[] transformed = new ProfTransformer().transform(null, "com.asm3.Comparable", null, null, bytes);
        System.out.println(transformed.length);
        System.out.println();

    }
}