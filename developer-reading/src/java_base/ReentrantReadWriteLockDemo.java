package java_base;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 读写锁：读锁的共享锁可保证并发读是非常高效的，读写，写读，写写的过程是互斥的。
 * 独占锁：指该锁一次只能被一个线程所持有。对ReentrantLock和Synchronized而言都是独占锁
 * 共享锁：指该锁可被多个线程所持有。
 * <p>
 * 小结：
 * 读-读可以共存
 * 读-写不能共存
 * 写-写不能共存
 * <p>
 * 写操作：原子+独占，整个过程必须是一个完整的统一体，中间不许被分割，被打断
 */
class MyCache2 {

    private volatile Map<String, Object> map = new HashMap<>();

    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    public void put(String key, Object value) {

        // 创建一个写锁
        rwLock.writeLock().lock();

        try {
            System.out.println(Thread.currentThread().getName() + "\t 正在写入：" + key);
            try {
                // 模拟网络拥堵，延迟0.3秒
                TimeUnit.MILLISECONDS.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            map.put(key, value);
            System.out.println(Thread.currentThread().getName() + "\t 写入完成");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 写锁 释放
            rwLock.writeLock().unlock();
        }
    }

    public void get(String key) {

        // 读锁
        rwLock.readLock().lock();
        try {
            System.out.println(Thread.currentThread().getName() + "\t 正在读取:");
            try {
                // 模拟网络拥堵，延迟0.3秒
                TimeUnit.MILLISECONDS.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Object value = map.get(key);
            System.out.println(Thread.currentThread().getName() + "\t 读取完成：" + value);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 读锁释放
            rwLock.readLock().unlock();
        }
    }

    public void clean() {
        map.clear();
    }

}

public class ReentrantReadWriteLockDemo {
    public static void main(String[] args) {

        MyCache2 myCache = new MyCache2();

        // 线程操作资源类，5个线程写
        for (int i = 1; i <= 5; i++) {
            // lambda表达式内部必须是final
            final int tempInt = i;
            new Thread(() -> {
                myCache.put(tempInt + "", tempInt + "");
            }, String.valueOf(i)).start();
        }

        // 线程操作资源类， 5个线程读
        for (int i = 1; i <= 5; i++) {
            // lambda表达式内部必须是final
            final int tempInt = i;
            new Thread(() -> {
                myCache.get(tempInt + "");
            }, String.valueOf(i)).start();
        }
    }
}
