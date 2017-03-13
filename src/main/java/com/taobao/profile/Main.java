/**
 * (C) 2011-2012 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * 
 */
package com.taobao.profile;

import com.taobao.profile.instrument.ProfTransformer;

import java.lang.instrument.Instrumentation;

/**
 * TProfiler入口
 * 
 * @author luqi
 * @since 2010-6-23
 */
public class Main {

	/**
	 * @param args
	 * @param inst
	 */
	public static void premain(String args, Instrumentation inst) {
		System.out.println("agent class is starting.......");
		Manager.instance().initialization();
		inst.addTransformer(new ProfTransformer());
		Manager.instance().startupThread();
	}
}
