package juc;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

public class LockAQS {
    private static Object objectLock = new Object();
    private static Lock lock = new ReentrantLock();


    public static void main(String[] args) {
//        testWaitAndNotify();
//        testLockAndUnLock();
        testParkAndUnPark();
    }

    static void testParkAndUnPark() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Thread t1 = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + "\t" + "come in");
            LockSupport.park();
            System.out.println(Thread.currentThread().getName() + "\t " + "被唤醒");
        });
        t1.start();

        new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + "\t " + "通知其他");
            LockSupport.unpark(t1);
        }).start();

    }

    static void testLockAndUnLock() {
        Condition condition = lock.newCondition();
        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                lock.lock();
                try {
                    System.out.println(Thread.currentThread().getName() + "\t" + " come in");
                    condition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName() + "\t " + "被唤醒");
            } finally {
                lock.unlock();
            }
        }, "LockA").start();

        new Thread(() -> {
            try {
                lock.lock();
                System.out.println(Thread.currentThread().getName() + "\t " + "通知其他");
                condition.signal();
            } finally {
                lock.unlock();
            }
        }, "LockB").start();
    }

    static void testWaitAndNotify() {
        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            synchronized (objectLock) {
                System.out.println(Thread.currentThread().getName() + "\t" + "come in");
                try {
                    objectLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName() + "\t " + "被唤醒");
            }
        }, "A").start();

        new Thread(() -> {
            synchronized (objectLock) {
                System.out.println(Thread.currentThread().getName() + "\t" + "通知其他");
                objectLock.notify();
            }
        }, "B").start();
    }
}