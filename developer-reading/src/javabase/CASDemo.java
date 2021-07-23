package javabase;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *  线程工作内存值与主内存值相同就更新，否则什么不做
 *   即：期望值与实际值比较，相同则更新不同则不变
 *   问题：存在ABA 问题？
 */
public class CASDemo {
    public static void main(String[] args) {
        AtomicInteger atomicInteger = new AtomicInteger(5);
        // true	 current data: 2019
        System.out.println(atomicInteger.compareAndSet(5, 2019) + "\t current data: " + atomicInteger.get());

        // false	 current data: 2019
        System.out.println(atomicInteger.compareAndSet(5, 1024) + "\t current data: " + atomicInteger.get());
    }
}
