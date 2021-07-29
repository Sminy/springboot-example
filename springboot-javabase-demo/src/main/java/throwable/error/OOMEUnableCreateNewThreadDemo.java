package throwable.error;

import java.util.concurrent.TimeUnit;

/**
 * java.lang.OutOfMemoryError:unable to create new native thread
 * 分析： 与平台操作系统有关
 *      1.应用创建了太多线程，一个应用进程创建多个线程，超过系统承载极限
 *      2.服务器并不允许你的应用程序创建这么多线程，linux系统默认运行单个进程可以创建的线程为1024个，如果应用创建超过这个数量报此异常
 */
public class OOMEUnableCreateNewThreadDemo {
    public static void main(String[] args) {
        for (int i = 0; ; i++) {
            System.out.println("************** i = " + i);
            new Thread(() -> {
                try {
                    TimeUnit.SECONDS.sleep(Integer.MAX_VALUE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, String.valueOf(i)).start();
        }
    }
}
