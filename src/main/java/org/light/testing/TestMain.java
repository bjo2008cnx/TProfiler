package org.light.testing;

/**
 * @author Michael.Wang
 * @date 2017/2/27
 */
    public class TestMain {

        public static void main(String[] args) throws InterruptedException {
            test3();
            while (true) {
                test();
            }
        }

    public static void test() throws InterruptedException {
        Thread.sleep(1000);
        System.out.println("test");
        test2();
    }

    public static void test2() throws InterruptedException {
        Thread.sleep(2000);
        System.out.println("test2");
    }

    public static void test3() throws InterruptedException {
        Thread.sleep(1000);
        System.out.println("test3");
    }
}
