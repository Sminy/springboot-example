package juc;

/**
 *  LockSupport是用来创建锁和其他同步类的基本线程阻塞原语。
 *  LockSupport中的park()和 unpark()的作用分别是阻塞线程和解除阻塞线程。
 *
 *3种让线程等待和唤醒的方法
 *
 * 方式1：使用Object中的wait()方法让线程等待，使用object中的notify()方法唤醒线程
 * 方式2：使用JUC包中Condition的await()方法让线程等待，使用signal()方法唤醒线程
 * 方式3：LockSupport类可以阻塞当前线程以及唤醒指定被阻塞的线程
 */
public class LockSupportDemo {

}
