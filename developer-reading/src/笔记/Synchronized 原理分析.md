## Synchronized 原理分析

###  1. jdk 为什么设计synchronized 关键字？

原因：因为在多线程环境中，有可能会出现多个线程同时访问同一个共享、可变资源的情况，这种资源可能是：对象、变量文件等。

- 共享：资源可以由多个线程同时访问
- 可变：资源可以在其生命周期内被修改

引出的问题：

​	由于线程执行的过程是不可控的，所以需要采取同步机制来协同对对象可变状态的访问。



### 2. 作用

Synchronized是Java中解决并发问题的一种最常用的方法，也是最简单的一种方法。

1. **原子性**：确保线程互斥的访问同步代码；
2. **可见性**：保证共享变量的修改能够及时可见，其实是通过Java内存模型中的 对一个变量unlock操作之前，必须要同步到主内存中；如果对一个变量进行lock操作，则将会清空工作内存中此变量的值，在执行引擎使用此变量前，需要重新从主内存中load操作或assign操作初始化变量值来保证的；
3. **有序性**：有效解决重排序问题，即 “一个unlock操作先行发生(happen-before)于后面对同一个锁的lock操作”；





Synchronized 属于jvm 自带的隐式锁；

显示锁：需要手动写代码加锁解锁

隐式锁：不需要手动加锁解锁



### 3. 锁对象

sync 锁对象：

- ​	修饰方法
  - 静态方法：锁定的是类
  - 非静态方法：锁定的是调用者

- ​	代码块：
  - 具体看锁定对象是否为同一个，注意Integer 范围 （-128 -127）

-  修饰变量





Synchronized总共有三种用法：

1. 当synchronized作用在实例方法时，监视器锁（monitor）便是对象实例（this）；

   ```
   private sync void move {  // 锁定实例对象
      ...
   }
   ```

   

2. 当synchronized作用在静态方法时，监视器锁（monitor）便是对象的Class实例，因为Class数据存在于永久代，因此静态方法锁相当于该类的一个全局锁；

   ```
   private static sync move {  // 锁定类对象
   	...
   }
   ```

   

3. 当synchronized作用在某一个对象实例时，监视器锁（monitor）便是括号括起来的对象实例  sync(this)

![图片](https://mmbiz.qpic.cn/mmbiz_png/SfAHMuUxqJ3aISBxMNeDoWYI3Q5jHTNoQxzIwj6icvic4Bw7iaPJibY52eMHfXzzfgTBHlglGsuwz8eADSgZNrPVIg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

### 4. 同步概念

#### Java对象头

在JVM中**，对象在内存中的布局分为三块区域：对象头、实例数据和对齐填充。**如下图所示：

![img](https://pic1.zhimg.com/v2-b17dcbd2d061b92980e2fa86a5995ef0_b.jpg)

1. 实例数据：存放类的属性数据信息，包括父类的属性信息；
2. 对齐填充：由于虚拟机要求 对象起始地址必须是8字节的整数倍。填充数据不是必须存在的，仅仅是为了字节对齐；
3. **对象头：Java对象头一般占有2个机器码（在32位虚拟机中，1个机器码等于4字节，也就是32bit，在64位虚拟机中，1个机器码是8个字节，也就是64bit），但是 如果对象是数组类型，则需要3个机器码，因为JVM虚拟机可以通过Java对象的元数据信息确定Java对象的大小，但是无法从数组的元数据来确认数组的大小，所以用一块来记录数组长度。**



Synchronized用的锁就是存在Java对象头里的，那么什么是Java对象头呢？Hotspot虚拟机的对象头主要包括两部分数据：**Mark Word（标记字段）、**Class Pointer（类型指针）。其中 Class Pointer是对象指向它的类元数据的指针，虚拟机通过这个指针来确定这个对象是哪个类的实例，Mark Word用于存储对象自身的运行时数据，它是实现轻量级锁和偏向锁的关键。 Java对象头具体结构描述如下：