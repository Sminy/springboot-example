## 线上OOM排查

### 1.原因

某Java服务（假设**PID=10765**）出现了OOM，最常见的原因为：

- 有可能是内存分配确实过小，而正常业务使用了大量内存
- 某一个对象被频繁申请，却没有释放，内存不断泄漏，导致内存耗尽
- 某一个资源被频繁申请，系统资源耗尽，例如：不断创建线程，不断发起网络连接

**总结**一下:  本身资源不够、申请资源太多、资源耗尽



### 2 异常 OutOfMemoryError

- OutOfMemoryError：PermGen space （方法区内存溢出）
- OutOfMemoryError：Java heap space （堆内存溢出）
- OutOfMemoryError：unable to create new native thread （创建的线程超过限制数）





####  线程：

> (MaxProcessMemory - JVMMemory - ReservedOsMemory) / (ThreadStackSize) = Number of threads

```
MaxProcessMemory         指的是一个进程的最大内存
JVMMemory                JVM内存
ReservedOsMemory         保留的操作系统内存
ThreadStackSize          线程栈的大小
```

##### 解决思路

- 超出服务器用户最大进程限制，通过以下命令可以查看（注意，不同用户，最大进程限制配置可能不一样）：ulimit  -u
- 如果程序中有bug，导致创建大量不需要的线程或者线程没有及时回收，那么必须解决这个bug，修改参数是不能解决问题的。
- 如果程序确实需要大量的线程，现有的设置不能达到要求，那么可以通过修改MaxProcessMemory，JVMMemory，ThreadStackSize这三个因素，来增加能创建的线程数：
  		MaxProcessMemory 使用64位操作系统
    		VMMemory 减少 JVMMemory 的分配
    		ThreadStackSize 减小单个线程的栈大小



**修改用户线程数限制**

1. 查看java进程pid

   ```shell
    ps -ef|grep java    // pid  = 22996
   ```

2. 根据pid查询允许的线程数

   ```shell
    pstree -p 22996 |wc -l  // 线程数 = 125
   ```

3. 修改线程数限制

   ```shell
   sudo vim /etc/security/limits.d/20-nproc.conf
   ```

   ![image-20210603095605049](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20210603095605049.png)



#### 2.1 内存泄露 和 内存溢出 的区别

##### 内存泄露[Memory Leak]

程序在申请内存后，无法释放已申请的内存空间，一次内存泄漏似乎不会有大的影响，但内存泄漏堆积后的后果就是内存溢出。

##### 内存溢出[Out Of Memory]

程序申请内存时，没有足够的内存供申请者使用，就是内存不够用，此时就会报错OOM，即所谓的内存溢出。

##### 两者关系

> 内存泄漏的堆积最终会导致内存溢出

##### 内存泄漏的分类

> 常发性内存泄漏

发生内存泄漏的代码会被多次执行到，每次被执行的时候都会导致一块内存泄漏。

> 偶发性内存泄漏

发生内存泄漏的代码只有在某些特定环境或操作过程下才会发生。常发性和偶发性是相对的。对于特定的环境，偶发性的也许就变成了常发性的。所以测试环境和测试方法对检测内存泄漏至关重要。

> 一次性内存泄漏

发生内存泄漏的代码只会被执行一次，或者由于算法上的缺陷，导致总会有一块仅且一块内存发生泄漏。比如，在类的构造函数中分配内存，在析构函数中却没有释放该内存，所以内存泄漏只会发生一次。

> 隐式内存泄漏

程序在运行过程中不停的分配内存，但是直到结束的时候才释放内存。严格的说这里并没有发生内存泄漏，因为最终程序释放了所有申请的内存。但是对于一个服务器程序，需要运行几天，几周甚至几个月，不及时释放内存也可能导致最终耗尽系统的所有内存。所以，称这类内存泄漏为隐式内存泄漏。

#### OOM的危害

> 应用服务异常
> 线程异常
> 程序崩溃
> 其他未知问题



#### 怎么解决

既然OOM这么恐怖，那么我们应该如何排查定位，并解决问题呢？

##### 定位进程

##### 模拟内存溢出

编写一段OutOfMemoryError测试代码，用来模拟出 OOM 场景。





### MAT

1. 根据pid生成快照信息

   ```
   jmap -dump:live,format=b,file=/home/heap.dump pid
   ```

2. 利用MAT工具 ——> 最大内存线程 ——> 右键 **Java Basics** -> **Thread Overview and Stacks**，会列表线程的堆栈信息

![image-20210603124206344](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20210603124206344.png)





### OOM总结

1. 要解决OOM异常或heap space的异常，一般的手段是首先通过内存映像分析工具（如Eclipse Memory Analyzer）对dump出来的堆转储快照进行分析，重点是确认内存中的对象是否是必要的，也就是要先分清楚到底是出现了内存泄漏（Memory Leak）还是内存溢出（Memory Overflow）
2. 如果是内存泄漏，可进一步通过工具查看泄漏对象到GC Roots的引用链。于是就能找到泄漏对象是通过怎样的路径与GCRoots相关联并导致垃圾收集器无法自动回收它们的。掌握了泄漏对象的类型信息，以及GCRoots引用链的信息，就可以比较准确地定位出泄漏代码的位置。
3. 如果不存在内存泄漏，换句话说就是内存中的对象确实都还必须存活着，那就应当检查虚拟机的堆参数（ `-Xmx`与 `-Xms`），与机器物理内存对比看是否还可以调大，从代码上检查是否存在某些对象生命周期过长、持有状态时间过长的情况，尝试减少程序运行期的内存消耗。