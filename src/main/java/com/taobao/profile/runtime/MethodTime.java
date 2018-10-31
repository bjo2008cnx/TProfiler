package com.taobao.profile.runtime;

import lombok.Data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 方法栈
 */
@Data
public class MethodTime {
    /**
     * 方法id
     */
    private long methodId;

    /**
     * 净耗时
     */
    private long useTime;

    /**
     * 毛耗时
     */
    private long rawTime;

    /**
     * 堆栈深度
     */
    private long stackNum;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 执行次数
     */
    private int executeTimes = 1;

    /**
     * 父节点
     */
    private MethodTime parent;

    /**
     * （紧邻的）被调用的方法时间
     */
    private List<MethodTime> children;

    public static final Comparator<? super MethodTime> USE_TIME_COMPARATOR = new Comparator<MethodTime>() {
        @Override
        public int compare(MethodTime o1, MethodTime o2) {
            return -Long.valueOf(o1.getUseTime()).compareTo(o2.getUseTime());
        }
    };

    public static final Comparator<? super MethodTime> RAW_TIME_COMPARATOR = new Comparator<MethodTime>() {
        @Override
        public int compare(MethodTime o1, MethodTime o2) {
            return -Long.valueOf(o1.getRawTime()).compareTo(o2.getRawTime());
        }
    };

    /**
     *
     * @param methodTime
     */
    public void sum(MethodTime methodTime) {
        if (methodTime != null){
            useTime += methodTime.getUseTime();
            rawTime += methodTime.getRawTime();
            executeTimes += methodTime.getExecuteTimes();
        }
    }

    /**
     * 增加子对象
     *
     * @param methodTime
     * @return
     */
    public List<MethodTime> addChild(MethodTime methodTime) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        //查找是否有相同id的child
        MethodTime foundObject = MethodTimeHelper.find(methodTime.getMethodId(), this.children);
        if (foundObject == null) {
            this.children.add(methodTime);
        }else{
            foundObject.sum(methodTime);  //如果有相同id的child,则累加
        }
        return this.children;
    }

}