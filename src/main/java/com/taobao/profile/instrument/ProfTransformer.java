/**
 * (C) 2011-2012 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 *
 */
package com.taobao.profile.instrument;

import com.taobao.profile.Profiler;
import com.taobao.profile.config.ProfFilter;
import com.taobao.profile.dependence_query.MysqlProfClassAdapter;
import com.taobao.profile.dependence_query.MysqlProfFilter;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * 自定义ClassFileTransformer,用于转换类字节码
 *
 * @author luqi
 * @since 2010-6-23
 */
public class ProfTransformer implements ClassFileTransformer {
    private static Logger log = LoggerFactory.getLogger(ProfTransformer.class);

    /**
     * 尝试对Mysql的包拦截
     *
     * @param loader
     * @param className
     * @param classBeingRedefined
     * @param protectionDomain
     * @param classfileBuffer
     * @return
     */
    private byte[] transform4Mysql(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[]
            classfileBuffer) {
        try {
            if (!MysqlProfFilter.getInstance().isNeedInject(className)) {
                return null;
            }
            log.debug("Inject class(mysql) :" + className);
            log.debug(" ---- TProfiler Debug: ClassLoader:" + loader + " ---- class: " + className + "  by mysqlASM");

            // 记录注入类数
            Profiler.instrumentClassCount.getAndIncrement();

            //使用asm修改类的字节码
            ClassReader reader = new ClassReader(classfileBuffer);
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            ClassAdapter adapter = new MysqlProfClassAdapter(writer, className);
            reader.accept(adapter, 0);
            // 生成新类字节码
            return writer.toByteArray();
        } catch (Exception e) {
            log.error("fail to transform for mysql",e);
        }
        return null;
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
            throws IllegalClassFormatException {
        if (loader != null && ProfFilter.isNotNeedInjectClassLoader(loader.getClass().getName())) {
            return classfileBuffer;
        }

        //如果可以注入mysql成功；则不再继续注入
//        byte[] temp = transform4Mysql(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
//        if (temp != null) {
//            return temp;
//        }

        if (!ProfFilter.isNeedInject(className)) {
            return classfileBuffer;
        }

        if (ProfFilter.isNotNeedInject(className)) {
            //log.warn("Do not need to  inject class :" + className);
            return classfileBuffer;
        }
        log.debug("Inject class :" + className);
        log.debug(" ---- TProfiler Debug: ClassLoader:" + loader + " ---- class: " + className);

        // 记录注入类数
        Profiler.instrumentClassCount.getAndIncrement();
        try {
            ClassReader reader = new ClassReader(classfileBuffer);
            log.debug("ClassReader is called.");
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            log.debug("ClassWriter is called.");
            ClassAdapter adapter = new ProfClassAdapter(writer, className);
            log.debug("ClassAdapter is called.");
            reader.accept(adapter, 0);
            // 生成新类字节码
            return writer.toByteArray();
        } catch (Throwable e) {
            log.error("fail to inject.",e);
            return classfileBuffer; // 返回旧类字节码
        }
    }
}
