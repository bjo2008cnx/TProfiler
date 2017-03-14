package com.taobao.profile.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

@Slf4j
public class ThreadUtil {

    /**
     * 休眠
     *
     * @param millSeconds 毫秒数
     */
    public static boolean sleep(long millSeconds) {
        try {
            Thread.sleep(millSeconds);
            return true;
        } catch (InterruptedException e) {
            log.warn("[WARNING]Sleeping is interrupted!!!");
            return false;
        }
    }

    /**
     * 获取线程id和名称
     *
     * @param t
     * @return
     */
    public static String getIdName(Thread t) {
        return "[" + t.getId() + ":" + t.getName() + "]";
    }

    /**
     * 挂起线程
     */
    public static void park() {
        log.debug("current thread is going to park");
        LockSupport.park();
        log.debug("current thread is parked");
    }

    /**
     * 解挂线程
     */
    public static void unpark(Thread t) {
        log.debug("current thread is going to unpark");
        LockSupport.unpark(t);
        log.debug("current thread is unparked");
    }

    /**
     * join到主线程
     *
     * @param threads
     */
    public static void join(Thread... threads) {
        for (Thread t : threads) {
            String idName = getIdName(t);
            log.debug("线程" + idName + "将join到主线程");
            try {
                t.join();
            } catch (InterruptedException e) {
                log.warn("线程" + idName + "join时被中断");
            }
            log.debug("线程" + idName + "joined 到主线程");
        }
    }

    /**
     * 创建一个固定大小的线程池并执行线程
     *
     * @param runnables
     */
    public static void execute(Runnable... runnables) {
        Executor executor = Executors.newFixedThreadPool(runnables.length);
        for (final Runnable r : runnables) {
            executor.execute(r);
        }
    }


    /**
     * 判断任务是否已完成
     *
     * @param tasks
     * @return
     */
    public static boolean isDone(RecursiveTask[] tasks) {
        for (RecursiveTask task : tasks) {
            if (!task.isDone()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 执行任务
     *
     * @param tasks
     * @param sleepSeconds
     */
    public static void execute(RecursiveTask[] tasks, int sleepSeconds) {
        ForkJoinPool pool = new ForkJoinPool();
        for (RecursiveTask task : tasks) {
            pool.execute(task);
        }

        do {
            log.info("******************************************\n");
            log.info("Main: Parallelism: " + pool.getParallelism());
            log.info("Main: Active Threads:" + pool.getActiveThreadCount());
            log.info("Main: Task Count:" + pool.getQueuedTaskCount());
            log.info("Main: Steal Count:" + pool.getStealCount());
            log.info("******************************************\n");

            ThreadUtil.sleep(sleepSeconds * 1000);
        } while (!ThreadUtil.isDone(tasks));
        pool.shutdown();
    }

    public static Thread findThreadByName(String threadName) {
        Thread[] list = findAllThreads();
        for (Thread thread : list) {
            if (thread.getName().equals(threadName)) {
                return thread;
            }
        }
        return null;
    }

    /**
     * @return
     */
    public static Thread[] findAllThreads() {
        ThreadGroup group = Thread.currentThread().getThreadGroup();
        ThreadGroup topGroup = group;
        // 遍历线程组树，获取根线程组
        while (group != null) {
            topGroup = group;
            group = group.getParent();
        }
        // 激活的线程数加倍
        int estimatedSize = topGroup.activeCount() * 2;
        Thread[] slackList = new Thread[estimatedSize];
        // 获取根线程组的所有线程
        int actualSize = topGroup.enumerate(slackList);
        // copy into a list that is the exact size
        Thread[] list = new Thread[actualSize];
        System.arraycopy(slackList, 0, list, 0, actualSize);
        return list;
    }

    /**
     * 等待
     *
     * @param latch
     * @param sleepTime
     */
    public static void await(CountDownLatch latch, long sleepTime) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            ThreadUtil.sleep(sleepTime);
        }
    }
}
