package com.taobao.profile.runtime;

import java.util.List;

/**
 * 方法栈
 */
public class MethodTimeHelper {
    /**
     * 将右侧对象中的时间叠加到左侧对象
     *
     * @param foundMethodTime
     * @param toAddMethodTime
     */
    public static void sumDetailTime(MethodTime foundMethodTime, MethodTime toAddMethodTime) {
        foundMethodTime.sum(toAddMethodTime); //当前节点时间累加
        List<MethodTime> foundChildren = foundMethodTime.getChildren();
        if (toAddMethodTime == null){
            return;
        }
        List<MethodTime> children = toAddMethodTime.getChildren();
        //子节点时间累加
        if (foundChildren != null && foundChildren.size() > 0) {
            for (MethodTime child : foundChildren) {
                MethodTime foundChild = MethodTimeHelper.find(child.getMethodId(), children);
                sumDetailTime(child, foundChild);
            }
        }
    }

    public static boolean equals(MethodTime a, MethodTime b) {
       throw new RuntimeException("TODO");
    }

    /**
     * 根据id查找
     *
     * @param methodId
     * @param list
     * @return
     */
    public static MethodTime find(long methodId, List<MethodTime> list) {
        if (list == null){
            return null;
        }
        for (MethodTime methodTime : list) {
            if (methodId == methodTime.getMethodId()) {
                return methodTime;
            }
        }
        return  null;
    }
}