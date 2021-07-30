package juc;

/**
 * 可重入锁： 又叫递归锁，同一个线程可以先后获取同一把锁
 * synchronized 可重入锁原理：
 * 每个锁对象拥有一个锁计数器和一个指向持有该锁的线程的指针。当执行monitorenter时，如果目标锁对象的计数器为零，那么说明它没有被其他线程所持有，Java虚拟机会将该锁对象的持有线程设置为当前线程，并且将其计数器加1。
 * 在目标锁对象的计数器不为零的情况下，如果锁对象的持有线程是当前线程，那么Java虚拟机可以将其计数器加1，否则需要等待，直至持有线程释放该锁。
 * 当执行monitorexit时，Java虚拟机则需将锁对象的计数器减1。计数器为零代表锁已被释放。
 *
 */
public class ReentrantLockDemo2 {
    public static void main(String[] args) {
        new ReentrantLockDemo2().m1();

    }

    public synchronized void m1() {
        System.out.println("===外");
        m2();
    }

    public synchronized void m2() {
        System.out.println("===中");
        m3();
    }

    public synchronized void m3() {
        System.out.println("===内");

    }
}
