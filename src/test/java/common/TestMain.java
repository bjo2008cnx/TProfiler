package common;

/**
 * @author Michael.Wang
 * @date 2017/2/27
 */
public class TestMain {

    public static void main(String[] args) throws InterruptedException {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                final TestMain testMain = new TestMain();
                try {
                    testMain.test();
                    testMain.test4();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();

        while (true) {
            StackTraceElement[] stacks = t.getStackTrace();
            int i = 0;
            for (StackTraceElement stack : stacks) {
                String output = String.format("Class:%s:%s:%s", stack.getClassName(), stack.getMethodName(), stack.getLineNumber());
                for (int k = 0; k < i; k++) {
                    System.out.print("  ");
                }
                i++;
                System.out.println(output);
            }
            Thread.sleep(100);
        }
    }

    public void test() throws InterruptedException {
        test2();
        Thread.sleep(1000);
        log("test");
    }

    public void test2() throws InterruptedException {
        test3();
        Thread.sleep(2000);
        log("test2");
    }

    public void test3() throws InterruptedException {
        Thread.sleep(1000);
        log("test3");
    }

    public void test4() throws InterruptedException {
        Thread.sleep(1000);
        log("test4");
    }

    private void log(String s) {
        // System.out.println(s);
    }
}
