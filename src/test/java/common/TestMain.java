package common;

/**
 * @author Michael.Wang
 * @date 2017/2/27
 */
public class TestMain {

    public static void main(String[] args) throws InterruptedException {
        TestMain testMain = new TestMain();
        testMain.test3();
        while (true) {
            testMain.test();
        }
    }

    public void test() throws InterruptedException {
        Thread.sleep(1000);
        System.out.println("test");
        new TestMain().test2();
    }

    public void test2() throws InterruptedException {
        Thread.sleep(2000);
        System.out.println("test2");
    }

    public void test3() throws InterruptedException {
        Thread.sleep(1000);
        System.out.println("test3");
    }
}
