## ThreadLocal  源码阅读篇

### 1. ThreadLocal 

在 ThreadLocal 源码实现中 ，涉及到了 ：数据结构、拉链存储、斐波那契数列、神奇的 0x61c88647、弱引用Reference、过期 key 探测清理等等。

- ThreadLocal提高一个线程的局部变量，访问某个线程拥有自己局部变量。
- 当使用ThreadLocal维护变量时，ThreadLocal为每个使用该变量的线程提供独立的变量副本，所以每一个线程都可以独立地改变自己的副本，而不会影响其它线程所对应的副本。
- threadlocal 作者：**Josh Bloch** and **Doug Lea**





![在这里插入图片描述](https://img-blog.csdnimg.cn/20210409114450730.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80NDA3NTEzMg==,size_16,color_FFFFFF,t_70)



#### 1.1 数据结构

![图片](https://mmbiz.qpic.cn/mmbiz_png/IoCLicsSWjprQcDzaqPfBLmhhVzM8tUCw4sicGuLVMMdict6tGv1TtNYsgaxpicyCKEMmgwWgj4BVJbTVmXzFGNz2g/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



1. 它是一个数组结构 Entry[] ,它的key是ThreadLocal<?> k ，继承自WeakReference， 也就是我们常说的弱引用类型
2. Thread类有一个类型为ThreadLocal.ThreadLocalMap的实例变量threadLocals，也就是说每个线程有一个自己的ThreadLocalMap。
3. 每个线程在往ThreadLocal里放值的时候，都会往自己的ThreadLocalMap里存，读也是以ThreadLocal作为引用，在自己的map里找对应的key，从而实现了线程隔离。
4. ThreadLocalMap有点类似HashMap的结构，只是HashMap是由数组+链表实现的，而ThreadLocalMap中并没有链表结构。





**ThreadLocal中的嵌套内部类ThreadLocalMap，这个类本质上是一个map，和HashMap之类的实现相似，依然是key-value的形式，其中有一个内部类Entry，其中key可以看做是ThreadLocal实例，但是其本质是持有ThreadLocal实例的弱引用**



```java
static class ThreadLocalMap {

        static class Entry extends WeakReference<ThreadLocal<?>> {
            /** The value associated with this ThreadLocal. */
            Object value;

            Entry(ThreadLocal<?> k, Object v) {
                super(k);
                value = v;
            }
        }

        /**
         * The initial capacity -- MUST be a power of two.
         */
        private static final int INITIAL_CAPACITY = 16;

        /**
         * The table, resized as necessary.
         * table.length MUST always be a power of two.
         */
        private Entry[] table;

        /**
         * The number of entries in the table.
         */
        private int size = 0;

        /**
         * The next size value at which to resize.
         */
        private int threshold; // Default to 0
        /**
         * Increment i modulo len.
         */
        private static int nextIndex(int i, int len) {
            return ((i + 1 < len) ? i + 1 : 0);
        }

  
        ThreadLocalMap(ThreadLocal<?> firstKey, Object firstValue) {
            table = new Entry[INITIAL_CAPACITY];
            int i = firstKey.threadLocalHashCode & (INITIAL_CAPACITY - 1);
            table[i] = new Entry(firstKey, firstValue);
            size = 1;
            setThreshold(INITIAL_CAPACITY);
        }
  ……   
      }
    }
```



#### 1.2 set 方法

```java
   public void set(T value) {
 	// 获取当前线程
    Thread t = Thread.currentThread();
    //获取当前map中是否存在 key 注：key就是当前线程
    ThreadLocalMap map = getMap(t);
    // map 不等于 null 调用 ThreadLocalMap 的 set(this, value);
    if (map != null)
        map.set(this, value);
    else
     // 在当前线程创建ThreadLocalMap
        createMap(t, value);
   }

void createMap(Thread t, T firstValue) {
    t.threadLocals = new `ThreadLocalMap`(this, firstValue);
}


--------------------------------------------------------------------------------------------


private void set(ThreadLocal<?> key, Object value) {
   //Entry，是一个弱引用对象的实现类，static class Entry extends WeakReference<ThreadLocal<?>>，
   // 以在没有外部强引用下，会发生GC，删除 key。
            Entry[] tab = table;
            int len = tab.length;
            //计算数组下标  hash算法
            int i = key.threadLocalHashCode & (len-1);
    // for循环快速找到插入位置
            for (Entry e = tab[i];
                 e != null;
                 //向后查找值
                 e = tab[i = nextIndex(i, len)]) {
                ThreadLocal<?> k = e.get();
    // 如果k已经有了，则直接覆盖                
    if (k == key) {
                    e.value = value;
                    return;
                }
    // 如果为 null
                if (k == null) {
                 // 替换过期数据的方法  ---- 下面讲解             
                 replaceStaleEntry(key, value, i);
                    return;
                }
            }
   // 设置值
            tab[i] = new Entry(key, value);
            int sz = ++size;
            // cleanSomeSlots 清除老旧的Entry（key == null）启发式清理）
            // 如果没有清理陈旧的 Entry 并且数组中的元素大于了阈值，则进行 rehash (扩容)
            if (!cleanSomeSlots(i, sz) && sz >= threshold)
             //扩容方法 ------后面讲
                rehash();
        }
```



#### 1.3 get  方法

```java
public T get() {
  //获取当前线程
        Thread t = Thread.currentThread();
        //获取 map 是否有值
        ThreadLocalMap map = getMap(t);
        //不等于空
        if (map != null) {
         //获取值
            ThreadLocalMap.Entry e = map.getEntry(this);
            // 如果不等于空直接返回
            if (e != null) {
                @SuppressWarnings("unchecked")
                T result = (T)e.value;
                return result;
            }
        }
        //设置初始化
        return setInitialValue();
    }
```



#### 1.4 ThreadLocal 为什么要用弱引用 ？



Entry便是ThreadLocalMap里定义的节点，它继承了WeakReference类，定义了一个类型为Object的value，用于存放塞到ThreadLocal里的值。

因为如果这里使用普通的key-value形式来定义存储结构，实质上就会造成节点的生命周期与线程强绑定，只要线程没有销毁，那么节点在GC分析中一直处于可达状态，没办法被回收，而程序本身也无法判断是否可以清理节点。弱引用是Java中四档引用的第三档，比软引用更加弱一些，如果一个对象没有强引用链可达，那么一般活不过下一次GC。当某个ThreadLocal已经没有强引用可达，则随着它被垃圾回收，在ThreadLocalMap里对应的Entry的键值会失效，这为ThreadLocalMap本身的垃圾清理提供了便利。

```java
static class Entry extends WeakReference<ThreadLocal<?>> {
            /** The value associated with this ThreadLocal. */
            Object value;

            Entry(ThreadLocal<?> k, Object v) {
                super(k);
                value = v;
            }
}
```

弱引用解释：

只具有弱引用的对象拥有更短暂的生命周期。在垃圾回收器线程扫描它所管辖的内存区域的过程中，一旦发现了只具有弱引用的对象，不管当前内存空间足够与否，都会回收它的内存。不过，由于垃圾回收器是一个优先级很低的线程，因此不一定会很快发现那些只具有弱引用的对象。

注意：WeakReference引用本身是强引用，它内部的（T reference）才是真正的弱引用字段，WeakReference就是一个装弱引用的容器而已。

为什么用弱引用：

```java
Thread -> ThreadLocal.ThreadLocalMap -> Entry[] -> Enrty -> key（threadLocal对象）和value
```

如上链路所示，这个链路全是强引用，当前线程还未结束时，他持有的都是强引用，包括递归下去的所有强引用都不会被垃圾回收器回收, 当把ThreadLocal对象的引用置为null后，没有任何强引用指向内存中的ThreadLocal实例，threadLocals的key是它的弱引用，故它将会被GC回收。下次，我们就可以通过Entry不为null，而key为null来判断该Entry对象该被清理掉了。



![图片](https://mmbiz.qpic.cn/mmbiz_jpg/gDHRjEowjKRPAap04cGBrno2bV5LJCqg3qOlGHw8wF7YYffAjbsSicVRYAMkQpeicGr6nia020fFEovrr9215OoZQ/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

**内存泄漏分析**

从上图可以看出，如果ThreadLocal没有外部强引用，当发生垃圾回收时，这个ThreadLocal一定会被回收(弱引用的特点是不管当前内存空间足够与否，GC时都会被回收)，这样就会导致ThreadLocalMap中出现key为null的Entry，外部将不能获取这些key为null的Entry的value，并且如果当前线程一直存活，那么就会存在一条强引用链：Thread Ref -> Thread -> ThreaLocalMap -> Entry -> value，导致value对应的Object一直无法被回收，产生内存泄露。

查看源码会发现，ThreadLocal的get、set和remove方法都实现了对所有key为null的value的清除，但仍可能会发生内存泄露，因为可能使用了ThreadLocal的get或set方法后发生GC，此后不调用get、set或remove方法，为null的value就不会被清除。

解决办法是每次使用完ThreadLocal都调用它的remove()方法清除数据，或者按照JDK建议将ThreadLocal变量定义成private static，这样就一直存在ThreadLocal的强引用，也就能保证任何时候都能通过ThreadLocal的弱引用访问到Entry的value值，进而清除掉。



- map 可以存key 为null 的键值对
- 弱引用特点是不管内存空间足够与否。GC时都会被回收





#### 1.5 Hash 算法

既然是Map结构，那么ThreadLocalMap当然也要实现自己的hash算法来解决散列表数组冲突问题。

```java
 private static final int HASH_INCREMENT = 0x61c88647;


 private static AtomicInteger nextHashCode =
        new AtomicInteger();


 private final int threadLocalHashCode = nextHashCode();



 private static int nextHashCode() {
         return nextHashCode.getAndAdd(HASH_INCREMENT);
    }
```

每当创建一个 ThreadLocal 对象 ，这个 ThreadLocal.nextHashCode  这个值就会增长  0x61c88647

这个值 ，它是斐波那契数  也叫 黄金分割线。为了让数据更加散列，减少 hash 碰撞



#### 1.6 应用场景

- 时间格式化
- spring 中的transactional  事务处理
- mybatis 分页插件

 **1、在进行对象跨层传递的时候，使用ThreadLocal可以避免多次传递，打破层次间的约束。**

 **2、线程间数据隔离**

 **3、进行事务操作，用于存储线程事务信息。**

 **4、数据库连接，Session会话管理。**

特点：

线程并发、传递数据、线程隔离



重写finalize会导致FGC 或OOM



#### 1.7  ThreadLocal内存泄漏

根本原因：

​		由于ThreadLocalMap的生命周期跟Thread一样长，如果没有手动删除对应的key就会导致内存泄漏。



#### 1.8 java中引用类型及特点

**强引用**：new 创建的对象关系

**软引用**：当内存不足，会触发JVM的GC，如果GC后，内存还是不足，就会把软引用的包裹的对象给干掉，也就是只有在内存不足，JVM才会回收该对象。

软引用到底有什么用呢？比较适合用作缓存，当内存足够，可以正常的拿到缓存，当内存不够，就会先干掉缓存，不至于马上抛出OOM。

```
SoftReference<Student>studentSoftReference=new SoftReference<Student>(new Student());
Student student = studentSoftReference.get();
System.out.println(student);
```



**弱引用**：弱引用的特点是不管内存是否足够，只要发生GC，都会被回收：

​	弱引用的使用和软引用类似，只是关键字变成了WeakReference：

​	弱引用在很多地方都有用到，比如ThreadLocal、WeakHashMap。

```
WeakReference<byte[]> weakReference = new WeakReference<byte[]>(new byte[1024\*1024\*10]);
System.out.println(weakReference.get());
```

**虚应用**：虚引用又被称为幻影引用：无法通过虚引用来获取对一个对象的真实引用。适用于堆外内存即直接内存的管理，NIO Netty

```
ReferenceQueue queue = new ReferenceQueue();
PhantomReference<byte[]> reference = new PhantomReference<byte[]>(new byte[1], queue);
System.out.println(reference.get());
```



**总结：**

强 引用: 最普通的引用 Object o = new Object()
软 引用: 垃圾回收器, 内存不够的时候回收 (缓存)
弱 引用: 垃圾回收器看见就会回收 (防止内存泄漏)
虚 引用: 垃圾回收器看见二话不说就回收,跟没有一样 (管理堆外内存) DirectByteBuffer -> 应用到NIO Netty

```
finalize(): 当对象被回收时, finalize()方法会被调用, 但是不推荐使用去回收一些资源,因为不知道他什么时候会被调用, 有时候不一定会调用
```

**总结: 强软弱虚** 

- 强 正常的引用
- 软 内存不够, 进行清除
  - 大对象的内存
  - 常用对象的缓存
- 弱 遇到GC就会被回收
  - 缓存, 没有容器引用指向的时候就需要清除缓存
  - ThreadLocal
  - WeakReferenceMap
- 虚 看见就回收, 且看不到值
  - 管理堆外内存

#### 1.9 ThreadLocal 和Synchronized的区别

虽然ThreadLocal模式与Synchronized关键字都用于处理多线程并发访问变量的问题, 不过两者处理问题的角度和思路不同

|        | synchronized                                                 | **ThreadLocal**                                              |
| ------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 原理   | 同步机制采用`以时间换空间`的方式,只提供了一份变量, 让不同的线程排队访问 | ThreadLocal采用`以空间换时间`的方式, 为每一个线程都提供了一份变量的副本, 从而实现同访问而相不干扰 |
| 侧重点 | 多个线程之间访问资源的同步                                   | 多线程中让每个线程之间的数据相互隔离                         |



#### 2.0 ThreadLocal 内部架构

**JDK8 优化设计(现在的设计)**

JDK8中ThreadLocal的设计是 : 每个Thread维护一个`ThreadLocalMap`, 这个Map的`key`是`ThreadLocal`实例本身,`value`才是真正要存储的值`Object`

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200422163908400.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2Jic2N6MDA3,size_16,color_FFFFFF,t_70)

具体过程如下:

```
1. 每个THreadLocal线程内部都有一个Map(ThreadLocalMap)

2. Map里面存储的ThreadLocal对象(key)和线程变量副本(Value)也就是存储的值

3. Thread内部的Map是由ThreadLocal维护的, 有THreadLocal负责向map获取和设置线程变量值

4. 对于不同的线程, 每次获取value(也就是副本值),别的线程并不能获取当前线程的副本值, 形成了副本的隔离,互不干扰.
```



#### 2.1 TheadLocal 源码分析

ThreadLocalMap是ThreadLocal的静态内部类, 没有实现Map接口, 用独立的方式实现了Map的功能, 其内部的Entry也是独立实现.

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200422164004917.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2Jic2N6MDA3,size_16,color_FFFFFF,t_70)



(ThreadLocal) key是弱引用, 其目的就是讲ThreadLocal对象的生命周期和和线程的生命周期解绑. 减少内存使用



#### 2.2 事务特点

- 同一个连接
- 事务隔离				

