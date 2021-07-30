package juc;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 可重入锁： 又叫递归锁，同一个线程可以先后获取同一把锁
 * 显式锁（即Lock）也有ReentrantLock这样的可重入锁
 */
public class ReentrantLockDemo3 {

    public static void main(String[] args) {
        Phone2 phone = new Phone2();

        /**
         * 因为Phone实现了Runnable接口
         */
        Thread t3 = new Thread(phone, "t3");
        Thread t4 = new Thread(phone, "t4");
        t3.start();
        t4.start();
    }

    static class Phone2 implements Runnable {

        Lock lock = new ReentrantLock();

        /**
         * set进去的时候，就加锁，调用set方法的时候，能否访问另外一个加锁的set方法
         */
        public void getLock() {
            lock.lock();
            try {
                System.out.println(Thread.currentThread().getName() + "\t get Lock");
                setLock();
            } finally {
                lock.unlock();
            }
        }

        public void setLock() {
            lock.lock();
            try {
                System.out.println(Thread.currentThread().getName() + "\t set Lock");
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void run() {
            getLock();
        }
    }
}
