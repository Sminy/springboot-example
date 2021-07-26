## Java 内存模型 JMM

### 1.JMM的产生背景

**硬件内存架构**

<img src="https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoeBothhI5TKLic6t7l9o8jG75e8rgJ4RCK8cpCa3VCicCVmibgjiaiaDszibBRzb4UGZgK9Zfdc4y1EdoA/640?wx_fmt=png&amp;tp=webp&amp;wxfrom=5&amp;wx_lazy=1&amp;wx_co=1" alt="图片" style="zoom:50%;" />

（1）CPU

去过机房的同学都知道，一般在大型服务器上会配置多个CPU，每个CPU还会有多个`核`，这就意味着多个CPU或者多个核可以同时（并发）工作。如果使用Java 起了一个多线程的任务，很有可能每个 CPU 都会跑一个线程，那么你的任务在某一刻就是真正并发执行了。

（2）CPU Register

CPU Register也就是 CPU 寄存器。CPU 寄存器是 CPU 内部集成的，在寄存器上执行操作的效率要比在主存上高出几个数量级。

（3）CPU Cache Memory

CPU Cache Memory也就是 CPU 高速缓存，相对于寄存器来说，通常也可以成为 L2 二级缓存。相对于硬盘读取速度来说内存读取的效率非常高，但是与 CPU 还是相差数量级，所以在 CPU 和主存间引入了多级缓存，目的是为了做一下缓冲。

（4）Main Memory

Main Memory 就是主存，主存比 L1、L2 缓存要大很多。



**缓存一致性问题**

由于主存与 CPU 处理器的运算能力之间有数量级的差距，所以在传统计算机内存架构中会引入高速缓存来作为主存和处理器之间的缓冲，CPU 将常用的数据放在高速缓存中，运算结束后 CPU 再讲运算结果同步到主存中。

使用高速缓存解决了 CPU 和主存速率不匹配的问题，但同时又引入另外一个新问题：缓存一致性问题。



<img src="https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoeBothhI5TKLic6t7l9o8jGYib0OVtNySicwoicw15kPCnJPhAhI5gib6pibmrf08ENnvpWstvGXrru8tw/640?wx_fmt=png&amp;tp=webp&amp;wxfrom=5&amp;wx_lazy=1&amp;wx_co=1" alt="图片" style="zoom:50%;" />

在多CPU的系统中(或者单CPU多核的系统)，每个CPU内核都有自己的高速缓存，它们共享同一主内存(Main Memory)。当多个CPU的运算任务都涉及同一块主内存区域时，CPU 会将数据读取到缓存中进行运算，这可能会导致各自的缓存数据不一致。

因此需要每个 CPU 访问缓存时遵循一定的协议，在读写数据时根据协议进行操作，共同来维护缓存的一致性。这类协议有 MSI、MESI、MOSI、和 Dragon Protocol 等。



**处理器优化和指令重排序**

为了提升性能在 CPU 和主内存之间增加了高速缓存，但在多线程并发场景可能会遇到`缓存一致性问题`。那还有没有办法进一步提升 CPU 的执行效率呢？答案是：处理器优化。

除了处理器会对代码进行优化处理，很多现代编程语言的编译器也会做类似的优化，比如像 Java 的即时编译器（JIT）会做指令重排序。

![图片](https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoeBothhI5TKLic6t7l9o8jGxH3tXq3XSCeC2J6giaKE8kicmeq75YqSAj7VpuRhp2H9bVy9YzCIOQzQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

```
处理器优化其实也是重排序的一种类型，这里总结一下，重排序可以分为三种类型：

编译器优化的重排序。编译器在不改变单线程程序语义放入前提下，可以重新安排语句的执行顺序。
指令级并行的重排序。现代处理器采用了指令级并行技术来将多条指令重叠执行。如果不存在数据依赖性，处理器可以改变语句对应机器指令的执行顺序。
内存系统的重排序。由于处理器使用缓存和读写缓冲区，这使得加载和存储操作看上去可能是在乱序执行。
```

**并发编程的问题**

<img src="https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoeBothhI5TKLic6t7l9o8jGCPjWA9V5WWEvQD71s4l7aiaAeJ7nU2ic2TJJ2HfGvuUuhXNZNuwWIvrA/640?wx_fmt=png&amp;tp=webp&amp;wxfrom=5&amp;wx_lazy=1&amp;wx_co=1" alt="图片" style="zoom:50%;" />

缓存一致性问题其实就是可见性问题，**处理器优化可能会造成原子性问题，指令重排序会造成有序性问题**，你看是不是都联系上了。

出了问题总是要解决的，那有什么办法呢？首先想到简单粗暴的办法，干掉缓存让 CPU 直接与主内存交互就解决了可见性问题，禁止处理器优化和指令重排序就解决了原子性和有序性问题，但这样一夜回到解放前了，显然不可取。

所以技术前辈们想到了在物理机器上定义出一套内存模型， 规范内存的读写操作。内存模型解决并发问题主要采用两种方式：**限制处理器优化**和**使用内存屏障**。



### 2.Java 内存模型

同一套内存模型规范，不同语言在实现上可能会有些差别。接下来着重讲一下 Java 内存模型实现原理。	

**Java 运行时内存区域与硬件内存的关系**

了解过 JVM 的同学都知道，JVM 运行时内存区域是分片的，分为栈、堆等，其实这些都是 JVM 定义的逻辑概念。在传统的硬件内存架构中是没有栈和堆这种概念。

<img src="https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoeBothhI5TKLic6t7l9o8jG2lCdPdkQOpFypCjCXfaqnChvsJLV9Ww6FJB3Vkia15csQYClcVMDajQ/640?wx_fmt=png&amp;tp=webp&amp;wxfrom=5&amp;wx_lazy=1&amp;wx_co=1" alt="图片" style="zoom:50%;" />

从图中可以看出栈和堆既存在于高速缓存中又存在于主内存中，所以两者并没有很直接的关系。

**Java 线程与主内存的关系**

Java 内存模型是一种规范，定义了很多东西：

- 所有的变量都存储在主内存（Main Memory）中。
- 每个线程都有一个私有的本地内存（Local Memory），本地内存中存储了该线程以读/写共享变量的拷贝副本。
- 线程对变量的所有操作都必须在本地内存中进行，而不能直接读写主内存。
- 不同的线程之间无法直接访问对方本地内存中的变量。

<img src="https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoeBothhI5TKLic6t7l9o8jGfiaKGmHML4iaAMgP8OGrEIs2VfIBJk8aQ6dwpQzXqf3zcibQWxEibDo76g/640?wx_fmt=png&amp;tp=webp&amp;wxfrom=5&amp;wx_lazy=1&amp;wx_co=1" alt="图片" style="zoom:50%;" />

​	**线程间通信**

如果两个线程都对一个共享变量进行操作，共享变量初始值为 1，每个线程都变量进行加 1，预期共享变量的值为 3。在 JMM 规范下会有一系列的操作。

<img src="https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoeBothhI5TKLic6t7l9o8jGoaFDDFDVJCib20GPP6BwHBibkgvF1RCbiaeTEiawwND3mvufe7MbqEWa0A/640?wx_fmt=png&amp;tp=webp&amp;wxfrom=5&amp;wx_lazy=1&amp;wx_co=1" alt="图片" style="zoom:50%;" />

为了更好的控制主内存和本地内存的交互，Java 内存模型定义了八种操作来实现：

- **lock**：锁定。作用于主内存的变量，把一个变量标识为一条线程独占状态。

- **unlock**：解锁。作用于主内存变量，把一个处于锁定状态的变量释放出来，释放后的变量才可以被其他线程锁定。

- **read**：读取。作用于主内存变量，把一个变量值从主内存传输到线程的工作内存中，以便随后的load动作使用

- **load**：载入。作用于工作内存的变量，它把read操作从主内存中得到的变量值放入工作内存的变量副本中。

- **use**：使用。作用于工作内存的变量，把工作内存中的一个变量值传递给执行引擎，每当虚拟机遇到一个需要使用变量的值的字节码指令时将会执行这个操作。

- **assign**：赋值。作用于工作内存的变量，它把一个从执行引擎接收到的值赋值给工作内存的变量，每当虚拟机遇到一个给变量赋值的字节码指令时执行这个操作。

- **store**：存储。作用于工作内存的变量，把工作内存中的一个变量的值传送到主内存中，以便随后的write的操作。

- **write**：写入。作用于主内存的变量，它把store操作从工作内存中一个变量的值传送到主内存的变量中。

  ```
  注意：工作内存也就是本地内存的意思。
  ```



Java内存模型还规定了在执行上述八种基本操作时，必须满足如下规则：

- 如果要把一个变量从主内存中复制到工作内存，就需要按顺寻地执行read和load操作， 如果把变量从工作内存中同步回主内存中，就要按顺序地执行store和write操作。但Java内存模型只要求上述操作必须按顺序执行，而没有保证必须是连续执行。
- 不允许read和load、store和write操作之一单独出现
- 不允许一个线程丢弃它的最近assign的操作，即变量在工作内存中改变了之后必须同步到主内存中。
- 不允许一个线程无原因地（没有发生过任何assign操作）把数据从工作内存同步回主内存中。
- 一个新的变量只能在主内存中诞生，不允许在工作内存中直接使用一个未被初始化（load或assign）的变量。即就是对一个变量实施use和store操作之前，必须先执行过了assign和load操作。
- 一个变量在同一时刻只允许一条线程对其进行lock操作，但lock操作可以被同一条线程重复执行多次，多次执行lock后，只有执行相同次数的unlock操作，变量才会被解锁。lock和unlock必须成对出现
- 如果对一个变量执行lock操作，将会清空工作内存中此变量的值，在执行引擎使用这个变量前需要重新执行load或assign操作初始化变量的值
- 如果一个变量事先没有被lock操作锁定，则不允许对它执行unlock操作；也不允许去unlock一个被其他线程锁定的变量。
- 对一个变量执行unlock操作之前，必须先把此变量同步到主内存中（执行store和write操作）。





**线程可见性**

解决这个内存可见性问题你可以使用：

- Java中的volatile关键字：volatile关键字可以保证直接从主存中读取一个变量，如果这个变量被修改后，总是会被写回到主存中去。Java内存模型是通过在变量修改后将新值同步回主内存，在变量读取前从主内存刷新变量值这种依赖主内存作为传递媒介的方式来实现可见性的，无论是普通变量还是volatile变量都是如此，普通变量与volatile变量的区别是：volatile的特殊规则保证了新值能立即同步到主内存，以及每个线程在每次使用volatile变量前都立即从主内存刷新。因此我们可以说volatile保证了多线程操作时变量的可见性，而普通变量则不能保证这一点。
- Java中的synchronized关键字：同步快的可见性是由“如果对一个变量执行lock操作，将会清空工作内存中此变量的值，在执行引擎使用这个变量前需要重新执行load或assign操作初始化变量的值”、“对一个变量执行unlock操作之前，必须先把此变量同步回主内存中（执行store和write操作）”这两条规则获得的。
- Java中的final关键字：final关键字的可见性是指，被final修饰的字段在构造器中一旦被初始化完成，并且构造器没有把“this”的引用传递出去（this引用逃逸是一件很危险的事情，其他线程有可能通过这个引用访问到“初始化了一半”的对象），那么在其他线程就能看见final字段的值（无须同步）



### **3.有态度的总结**

由于CPU 和主内存间存在数量级的速率差，想到了引入了多级高速缓存的传统硬件内存架构来解决，多级高速缓存作为 CPU 和主内间的缓冲提升了整体性能。解决了速率差的问题，却又带来了缓存一致性问题。

数据同时存在于高速缓存和主内存中，如果不加以规范势必造成灾难，因此在传统机器上又抽象出了内存模型。

Java 语言在遵循内存模型的基础上推出了 JMM 规范，目的是解决由于多线程通过共享内存进行通信时，存在的本地内存数据不一致、编译器会对代码指令重排序、处理器会对代码乱序执行等带来的问题。

为了更精准控制工作内存和主内存间的交互，JMM 还定义了八种操作：`lock`, `unlock`, `read`, `load`,`use`,`assign`, `store`, `write`。