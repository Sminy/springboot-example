package base.concurrent;

import java.util.concurrent.TimeUnit;

/**
 * 多线程死锁： 是指两个或两个以上的进程在执行过程中，因争夺资源而造成的一种互相等待的现象，若无外力作用，它们都将无法推进下去。
 * <p>
 * 产生死锁主要原因：
 * （1）系统资源不足
 * （2）进程运行推进的顺序不合适
 * （3）资源分配不当
 * <p>
 * 发生死锁的四个条件：
 * （1）互斥条件，线程使用的资源至少有一个不能共享的。
 * (2)至少有一个线程必须持有一个资源且正在等待获取一个当前被别的线程持有的资源。
 * (3)资源不能被抢占。
 * (4)循环等待。
 * <p>
 * 如何解决死锁问题 ？
 * 破坏发生死锁的四个条件其中之一即可。
 */
public class DeadLockDemo {
    public static void main(String[] args) {
        Object resourceA = new Object();
        Object resourceB = new Object();

        new Thread(new MyTask(resourceA, resourceB), "Thread A").start();
        new Thread(new MyTask(resourceB, resourceA), "Thread B").start();
    }

}

class MyTask implements Runnable {

    private Object resourceA, resourceB;

    public MyTask(Object resourceA, Object resourceB) {
        this.resourceA = resourceA;
        this.resourceB = resourceB;
    }

    @Override
    public void run() {
        synchronized (resourceA) {
            System.out.println(String.format("%s 自己持有%s，尝试持有%s",
                    Thread.currentThread().getName(), resourceA, resourceB));

            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            synchronized (resourceB) {
                System.out.println(String.format("%s 同时持有%s，%s",
                        Thread.currentThread().getName(), resourceA, resourceB));
            }
        }
    }
}
