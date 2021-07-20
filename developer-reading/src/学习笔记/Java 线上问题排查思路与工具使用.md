## Java 线上问题排查思路与工具使用

### 一、前言

由于业务应用 bug(本身或引入第三方库)、环境原因、硬件问题等原因，Java 线上服务出现故障 / 问题几乎不可避免。例如，常见的现象包括部分请求超时、用户明显感受到系统发生卡顿等等。

尽快线上问题从系统表象来看非常明显，但排查深究其发生的原因还是比较困难的，因此对开发测试或者是运维的同学产生了许多的困扰。

排查定位线上问题是具有一定技巧或者说是经验规律的，排查者如果对业务系统了解得越深入，那么相对来说定位也会容易一些。

不管怎么说，掌握 Java 服务线上问题排查思路并能够熟练排查问题常用工具 / 命令 / 平台是每一个 Java 程序猿进阶必须掌握的实战技能。

笔者依据自己的 工作经验总结出一套基本的线上问



### 二、Java 服务常见线上问题

所有 Java 服务的线上问题从系统表象来看归结起来总共有四方面：**CPU、内存、磁盘、网络**。例如 CPU 使用率峰值突然飚高、内存溢出 (泄露)、磁盘满了、网络流量异常、FullGC 等等问题。

基于这些现象我们可以将线上问题分成两大类:  **系统异常、业务服务异常**。

#### 1. 系统异常

常见的系统异常现象包括:  CPU 占用率过高、CPU 上下文切换频率次数较高、磁盘满了、磁盘 I/O 过于频繁、网络流量异常 (连接数过多)、系统可用内存长期处于较低值 (导致 oom killer) 等等。

这些问题可以通过 top(cpu)、free(内存)、df(磁盘)、dstat(网络流量)、pstack、vmstat、strace(底层系统调用) 等工具获取系统异常现象数据。

内存查看：**free  -m -s 10**

```shell
free [options]
-b #以Byte为单位显示内存使用情况
-k #以KB为单位显示内存使用情况
-m #以MB为单位显示内存使用情况
-g #以GB为单位显示内存使用情况
-o #不显示缓冲区调节列
-s<间隔秒数> #持续观察内存使用状况
-t #显示内存总和列
-V #显示版本信息
```

**磁盘：df** :用于显示磁盘的相关信息。df（Disk Free）的首字母组合，用来显示文件系统磁盘空间的使用情况

网络：**netstat**

```shell
-a或--all    #显示所有的网络连接信息
-A<网络类型>  #显示该网络类型连线中的相关地址
-c或--continuous  #持续列出网络状态信息
-C或--cache  #显示路由器配置的快取信息
-e或--extend  #显示网络其他相关信息
-g或--groups  #显示多播功能群组信息
-h或--help    #打印在线帮助信息
-i或--interfaces  #显示网络界面信息表单
-l或--listening   #显示监控中的服务器的Socket
-M或--masquerade  #显示伪装的网络连线
-n或--numeric     #直接使用ip地址
-N或--netlink    #显示网络硬件外围设备的符号连接名称
-o或--timers     #显示计时器
-r或--route      #显示Routing Table
-s或--statistice   #显示所有端口的状态统计信息
-t或--tcp   #显示TCP传输协议的连接状态
-u或--udp   #显示UDP传输协议的连接状态
-v或--verbose  #显示指令执行过程信息
-V或--version  #显示版本信息
-w或--raw      #显示RAW传输协议的连线状况
-x或--unix    #此参数与"-A unix"参数结果相同
--ip或--inet  #此参数与"-A inet"参数结果 相同
```

此外，如果对系统以及应用进行排查后，均未发现异常现象的更笨原因，那么也有可能是外部基础设施如 IAAS 平台本身引发的问题。

例如运营商网络或者云服务提供商偶尔可能也会发生一些故障问题，你的引用只有某个区域如广东用户访问系统时发生服务不可用现象，那么极有可能是这些原因导致的。

#### 2. 业务服务异常

常见的业务服务异常现象包括: PV 量过高、服务调用耗时异常、线程死锁、多线程并发问题、频繁进行 Full GC、异常安全攻击扫描等。

### 三、问题定位

我们一般会采用排除法，从外部排查到内部排查的方式来定位线上服务问题。

- 首先我们要排除其他进程 (除主进程之外) 可能引起的故障问题；
- 然后排除业务应用可能引起的故障问题；
- 可以考虑是否为运营商或者云服务提供商所引起的故障。

#### 1. 定位流程

**1.1 系统异常排查流程**

![图片](http://mmbiz.qpic.cn/mmbiz_png/0vU1ia3htaaMCzibBMVpstibWpOMeg5vzickibaepcyYpVfbUTW1PtckLRqeibohEgWcoyVNVVQl7TPzoU82L2QKIGlw/?wx_fmt=gif&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

**1.2 业务应用排查流程**

![图片](http://mmbiz.qpic.cn/mmbiz_png/0vU1ia3htaaMCzibBMVpstibWpOMeg5vzickKd6eiceTcuc89icicv2EUVMTgepbn5b3IaopQFr2CF5A5JmCu0rrYGkQQ/?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

#### 2. Linux 常用的性能分析工具

Linux 常用的性能分析工具使用包括 : top(cpu)、free(内存)、df(磁盘)、dstat(网络流量)、pstack、vmstat、strace(底层系统调用) 等。

**2.1 CPU**

CPU 是系统重要的监控指标，能够分析系统的整体运行状况。监控指标一般包括运行队列、CPU 使用率和上下文切换等。

top 命令是 Linux 下常用的 CPU 性能分析工具 , 能够实时显示系统中各个进程的资源占用状况 , 常用于服务端性能分析。

![图片](http://mmbiz.qpic.cn/mmbiz_png/0vU1ia3htaaMCzibBMVpstibWpOMeg5vzickR2oIZic5lRnyImBbjt4yPvn9wbZqiaSp9lRia2xiaNq1H62ia8YULJt1n1w/?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

```
top 命令显示了各个进程 CPU 使用情况 , 一般 CPU 使用率从高到低排序展示输出。其中 Load Average 显示最近 1 分钟、5 分钟和 15 分钟的系统平均负载，上图各值为 2.46，1.96，1.99。

我们一般会关注 CPU 使用率最高的进程，正常情况下就是我们的应用主进程。第七行以下：各进程的状态监控。
```

```
PID : 进程 id
USER : 进程所有者
PR : 进程优先级
NI : nice 值。负值表示高优先级，正值表示低优先级
VIRT : 进程使用的虚拟内存总量，单位 kb。VIRT=SWAP+RES
RES : 进程使用的、未被换出的物理内存大小，单位 kb。RES=CODE+DATA
SHR : 共享内存大小，单位 kb
S : 进程状态。D= 不可中断的睡眠状态 R= 运行 S= 睡眠 T= 跟踪 / 停止 Z= 僵尸进程
%CPU : 上次更新到现在的 CPU 时间占用百分比
%MEM : 进程使用的物理内存百分比
TIME+ : 进程使用的 CPU 时间总计，单位 1/100 秒
COMMAND : 进程名称
```

**2.2 内存**

内存是排查线上问题的重要参考依据，内存问题很多时候是引起 CPU 使用率较高的见解因素。

系统内存：free 是显示的当前内存的使用 ,-m 的意思是 M 字节来显示内容。

```
free -m
```

部分参数说明：

```
 total 内存总数: 3790M
  used 已经使用的内存数: 1880M
  free 空闲的内存数: 118M
  shared 当前已经废弃不用 , 总是 0
  buffers Buffer 缓存内存数: 1792M
```

**2.3 磁盘**

```
df -h
```

**2.4 网络**

dstat 命令可以集成了 vmstat、iostat、netstat 等等工具能完成的任务。

```
 dstat -c  cpu 情况
    -d 磁盘读写
        -n 网络状况
        -l 显示系统负载
        -m 显示形同内存状况
        -p 显示系统进程信息
        -r 显示系统 IO 情况
```

**2.5 其它**

vmstat：

```
vmstat 2 10 -t
```

vmstat 是 Virtual Meomory Statistics（虚拟内存统计）的缩写 , 是实时系统监控工具。该命令通过使用 knlist 子程序和 /dev/kmen 伪设备驱动器访问这些数据，输出信息直接打印在屏幕。

使用 vmstat 2 10  -t 命令，查看 io 的情况 (第一个参数是采样的时间间隔数单位是秒，第二个参数是采样的次数)。

![图片](http://mmbiz.qpic.cn/mmbiz_png/0vU1ia3htaaMCzibBMVpstibWpOMeg5vzick0bbUZdoFahubZ0e7OeHOl2zdYotR0icib0vPLJCl6YodjTW4u9c3Jwzw/?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



```
r 表示运行队列 (就是说多少个进程真的分配到 CPU),b 表示阻塞的进程。    
swpd 虚拟内存已使用的大小，如果大于 0，表示你的机器物理内存不足了，如果不是程序内存泄露的原因，那么你该升级内存了或者把耗内存的任务迁移到其他机器。
free   空闲的物理内存的大小，我的机器内存总共 8G，剩余 3415M。
buff   Linux/Unix 系统是用来存储，目录里面有什么内容，权限等的缓存，我本机大概占用 300 多 M
cache 文件缓存
si 列表示由磁盘调入内存，也就是内存进入内存交换区的数量；
so 列表示由内存调入磁盘，也就是内存交换区进入内存的数量
一般情况下，si、so 的值都为 0，如果 si、so 的值长期不为 0，则表示系统内存不足，需要考虑是否增加系统内存。    
bi 从块设备读入数据的总量（读磁盘）（每秒 kb）
bo 块设备写入数据的总量（写磁盘）（每秒 kb）
随机磁盘读写的时候，这两个值越大 ((超出 1024k)，能看到 cpu 在 IO 等待的值也会越大
这里设置的 bi+bo 参考值为 1000，如果超过 1000，而且 wa 值比较大，则表示系统磁盘 IO 性能瓶颈。
in 每秒 CPU 的中断次数，包括时间中断
cs(上下文切换 Context Switch)
```

strace：strace 常用来跟踪进程执行时的系统调用和所接收的信号。

```
strace -cp tid
strace -T -p tid
    -T 显示每一调用所耗的时间 .
    -p pid  跟踪指定的进程 pid.
    -v 输出所有的系统调用 . 一些调用关于环境变量 , 状态 , 输入输出等调用由于使用频繁 , 默认不输出 .
    -V 输出 strace 的版本信息 .
```

![图片](http://mmbiz.qpic.cn/mmbiz_png/0vU1ia3htaaMCzibBMVpstibWpOMeg5vzick2ibiaqt0HtDZy38vvtFoT7HU5pkI0IX7jxvnMhhDiavYfDqrPicRtPL2qA/?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



#### 3. JVM 定位问题工具

在 JDK 安装目录的 bin 目录下默认提供了很多有价值的命令行工具。每个小工具体积基本都比较小，因为这些工具只是 jdk\lib\tools.jar 的简单封装。

其中，定位排查问题时最为常用命令包括:jps（进程）、jmap（内存）、jstack（线程）、jinfo(参数) 等。

- jps: 查询当前机器所有 JAVA 进程信息；
- jmap: 输出某个 java 进程内存情况 (如:产生那些对象及数量等)；
- jstack: 打印某个 Java 线程的线程栈信息；
- jinfo: 用于查看 jvm 的配置参数。

**3.1 jps 命令**

jps 用于输出当前用户启动的所有进程 ID，当线上发现故障或者问题时，能够利用 jps 快速定位对应的 Java 进程 ID。

```
jps -l -m
-m -l -l 参数用于输出主启动类的完整路径
```

![图片](http://mmbiz.qpic.cn/mmbiz_png/0vU1ia3htaaMCzibBMVpstibWpOMeg5vzickbgh9fO52oy62O6Nz0qHpPJWOAialyuj96LGxNtLEJq6QqE0P5Y5wf5Q/?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

当然，我们也可以使用 Linux 提供的查询进程状态命令，例如：

```
ps -ef | grep tomcat
```

我们也能快速获取 tomcat 服务的进程 id。

**3.2 jmap 命令**

```
jmap -heap pid   输出当前进程 JVM 堆新生代、老年代、持久代等请情况，GC 使用的算法等信息
jmap -histo:live {pid} | head -n 10  输出当前进程内存中所有对象包含的大小
jmap -dump:format=b,file=/usr/local/logs/gc/dump.hprof {pid} 以二进制输出档当前内存的堆情况，然后可以导入 MAT 等工具进行
```

jmap(Java Memory Map) 可以输出所有内存中对象的工具 , 甚至可以将 VM 中的 heap, 以二进制输出成文本。

jmap -heap pid：

```
jmap -heap pid   输出当前进程 JVM 堆新生代、老年代、持久代等请情况，GC 使用的算法等信息
```

jmap 可以查看 JVM 进程的内存分配与使用情况，使用 的 GC 算法等信息。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/0vU1ia3htaaMCzibBMVpstibWpOMeg5vzickNicO7dnt1sPasHdBknvpA9anFichXic4VU0hHhV2JRC2vmzYtokID0Hww/?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

jmap -histo:live {pid} | head -n 10：

```
jmap -histo:live {pid} | head -n 10  输出当前进程内存中所有对象包含的大小
```

输出当前进程内存中所有对象实例数 (instances) 和大小 (bytes), 如果某个业务对象实例数和大小存在异常情况，可能存在内存泄露或者业务设计方面存在不合理之处。

jmap -dump：

```
jmap -dump:format=b,file=/usr/local/logs/gc/dump.hprof {pid}
```

-dump:formate=b,file= 以二进制输出当前内存的堆情况至相应的文件，然后可以结合 MAT 等内存分析工具深入分析当前内存情况。

一般我们要求给 JVM 添加参数 `-XX:+Heap Dump On Out Of Memory Error OOM` 确保应用发生 OOM 时 JVM 能够保存并 dump 出当前的内存镜像。

当然，如果你决定手动 dump 内存时，dump 操作占据一定 CPU 时间片、内存资源、磁盘资源等，因此会带来一定的负面影响。

此外，dump 的文件可能比较大 , 一般我们可以考虑使用 zip 命令对文件进行压缩处理，这样在下载文件时能减少带宽的开销。

下载 dump 文件完成之后，由于 dump 文件较大可将 dump 文件备份至制定位置或者直接删除，以释放磁盘在这块的空间占用。

**3.3 jstack 命令**

```
printf '%x\n' tid   -->  10 进制至 16 进制线程 ID(navtive 线程) %d 10 进制
jstack pid | grep tid -C 30 --color
ps -mp 8278 -o THREAD,tid,time | head -n 40
```

某 Java 进程 CPU 占用率高，我们想要定位到其中 CPU 占用率最高的线程。

(1) 利用 top 命令可以查出占 CPU 最高的线程 pid

```
top -Hp {pid}
```

![图片](http://mmbiz.qpic.cn/mmbiz_png/0vU1ia3htaaMCzibBMVpstibWpOMeg5vzickQqwVCPicNl6ic2pJIbkOn4GQBbB7k1jBCLL3yarSucIRBQu0C32DAXfg/?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

(2) 占用率最高的线程 ID 为 6900，将其转换为 16 进制形式 (因为 java native 线程以 16 进制形式输出)

```
printf '%x\n' 6900
```

![图片](http://mmbiz.qpic.cn/mmbiz_png/0vU1ia3htaaMCzibBMVpstibWpOMeg5vzickXloStlPUutB0XhSt1pel9zPHhWeKRXOfR2X5xAaexCdUrBNibd3VSUQ/?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

(3) 利用 jstack 打印出 java 线程调用栈信息

```
jstack 6418 | grep '0x1af4' -A 50 --color
```

**![图片](http://mmbiz.qpic.cn/mmbiz_png/0vU1ia3htaaMCzibBMVpstibWpOMeg5vzickCNa9Y27wPnnUZndt4652qpQuueLBD82lydlDXfRsMbiaRHcGwohLBGw/?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)**

**3.4 jinfo 命令**

```
查看某个 JVM 参数值
jinfo -flag ReservedCodeCacheSize 28461
jinfo -flag MaxPermSize 28461
```

**3.5 jstat 命令**

```
jstat -gc pid
jstat -gcutil `pgrep -u admin java`
```

#### 4. 内存分析工具 MAT

**4.1 什么是 MAT?**

MAT(Memory Analyzer Tool)，一个基于 Eclipse 的内存分析工具，是一个快速、功能丰富的 JAVA heap 分析工具，它可以帮助我们查找内存泄漏和减少内存消耗。

使用内存分析工具从众多的对象中进行分析，快速的计算出在内存中对象的占用大小，看看是谁阻止了垃圾收集器的回收工作，并可以通过报表直观的查看到可能造成这种结果的对象。

![图片](http://mmbiz.qpic.cn/mmbiz_png/0vU1ia3htaaMCzibBMVpstibWpOMeg5vzicks5iaHicSUZAu11IBRhiaE1Nj5pyqS5mKUsVODYxyOY0ibfk3xUNxA4DrJQ/?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

右侧的饼图显示当前快照中最大的对象。单击工具栏上的柱状图，可以查看当前堆的类信息，包括类的对象数量、浅堆 (Shallow heap)、深堆 (Retained Heap).

**浅堆**表示一个对象结构所占用内存的大小。**深堆**表示一个对象被回收后，可以真实释放的内存大小。

1）支配树 (The Dominator Tree)

列出了堆中最大的对象，第二层级的节点表示当被第一层级的节点所引用到的对象，当第一层级对象被回收时，这些对象也将被回收。

这个工具可以帮助我们定位对象间的引用情况，垃圾回收时候的引用依赖关系

2）Path to GC Roots

被 JVM 持有的对象，如当前运行的线程对象，被 systemclass loader 加载的对象被称为 GC Roots， 从一个对象到 GC Roots 的引用链被称为 Path to GC Roots。

通过分析 Path to GC Roots 可以找出 JAVA 的内存泄露问题，当程序不在访问该对象时仍存在到该对象的引用路径。

### 四、日志分析

#### 1. GC 日志分析

**1.1 GC 日志详细分析**

Java 虚拟机 GC 日志是用于定位问题重要的日志信息，频繁的 GC 将导致应用吞吐量下降、响应时间增加，甚至导致服务不可用。

```
-XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:/usr/local/gc/gc.log -XX:+UseConcMarkSweepGC
```

我们可以在 java 应用的启动参数中增加 **-XX:+PrintGCDetails** 可以输出 GC 的详细日志，例外还可以增加其他的辅助参数，如-Xloggc 制定 GC 日志文件地址。如果你的应用还没有开启该参数 , 下次重启时请加入该参数。

![图片](http://mmbiz.qpic.cn/mmbiz_png/0vU1ia3htaaMCzibBMVpstibWpOMeg5vzickRGiaFE7jWvNK5VeHDejk3eQLmRrGGAaunyYVQ4eibJibVhriaKZux3Yg7g/?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

上图为线上某应用在平稳运行状态下的 GC 日志截图。

```
2017-12-29T18:25:22.753+0800: 73143.256: [GC2017-12-29T18:25:22.753+0800: 73143.257: [ParNew: 559782K->1000K(629120K), 0.0135760 secs] 825452K->266673K(2027264K), 0.0140300 secs] [Times: user=0.02 sys=0.00, real=0.02 secs] [2017-12-29T18:25:22.753+0800: 73143.256] ： 自JVM启动73143.256秒时发生本次GC.
[ParNew: 559782K->1000K(629120K), 0.0135760 secs] : 对新生代进行的GC，使用ParNew收集器，559782K是新生代回收前的大小,1000K是新生代回收后大小,629120K是当前新生代分配的内存总大小, 0.0135760 secs表示本次新生代回收耗时 0.0135760秒
[825452K->266673K(2027264K), 0.0140300 secs]:825452K是回收堆内存大小,266673K是回收堆之后内存大小，2027264K是当前堆内存总大小,0.0140300 secs表示本次回收共耗时0.0140300秒
[Times: user=0.02 sys=0.00, real=0.02 secs] : 用户态耗时0.02秒,系统态耗时0.00,实际耗时0.02秒
```

无论是 minor GC 或者是 Full GC, 我们主要关注 GC 回收实时耗时 , 如 real=0.02secs, 即 stop the world 时间，如果该时间过长，则严重影响应用性能。

**1.2 CMS GC 日志分析**

Concurrent Mark Sweep(CMS) 是老年代垃圾收集器 , 从名字 (Mark Sweep) 可以看出，CMS 收集器就是 “标记-清除” 算法实现的，分为六个步骤：

- 初始标记 (STW initial mark)；
- 并发标记 (Concurrent marking)；
- 并发预清理 (Concurrent precleaning)；
- 重新标记 (STW remark)；
- 并发清理 (Concurrent sweeping)；
- 并发重置 (Concurrent reset)。

其中初始标记 (STW initial mark) 和 重新标记 (STW remark) 需要”Stop the World”。

**初始标记** ：在这个阶段，需要虚拟机停顿正在执行的任务，官方的叫法 STW(Stop The Word)。这个过程从垃圾回收的 “ 根对象 “ 开始，只扫描到能够和 “ 根对象 “ 直接关联的对象，并作标记。

所以这个过程虽然暂停了整个 JVM，但是很快就完成了。

**并发标记** ：这个阶段紧随初始标记阶段，在初始标记的基础上继续向下追溯标记。并发标记阶段，应用程序的线程和并发标记的线程并发执行，所以用户不会感受到停顿。

**并发预清理** ：并发预清理阶段仍然是并发的。在这个阶段，虚拟机查找在执行并发标记阶段新进入老年代的对象 (可能会有一些对象从新生代晋升到老年代， 或者有一些对象被分配到老年代)。

通过重新扫描，减少下一个阶段 “ 重新标记 “ 的工作，因为下一个阶段会 Stop The World。

**重新标记** ：这个阶段会暂停虚拟机，收集器线程扫描在 CMS 堆中剩余的对象。扫描从 “ 跟对象 “ 开始向下追溯，并处理对象关联。

**并发清理** ：清理垃圾对象，这个阶段收集器线程和应用程序线程并发执行。

**并发重置** ：这个阶段，重置 CMS 收集器的数据结构，等待下一次垃圾回收。

cms 使得在整个收集的过程中只是很短的暂停应用的执行 , 可通过在 JVM 参数中设置 **-XX:UseConcMarkSweepGC** 来使用此收集器 , 不过此收集器仅用于 old 和 Perm(永生) 的对象收集。

CMS 减少了 stop the world 的次数，不可避免地让整体 GC 的时间拉长了。

Full GC 的次数说的是 stop the world 的次数，所以一次 CMS 至少会让 Full GC 的次数 +2，因为 CMS Initial mark 和 remark 都会 stop the world，记做 2 次。而 CMS 可能失败再引发一次 Full GC。

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

上图为线上某应用在进行 CMS GC 状态下的 GC 日志截图。

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

如果你已掌握 CMS 的垃圾收集过程，那么上面的 GC 日志你应该很容易就能看的懂，这里我就不详细展开解释说明了。

此外 CMS 进行垃圾回收时也有可能会发生失败的情况。

异常情况有：

1）伴随 prommotion failed, 然后 Full GC：

[prommotion failed：存活区内存不足，对象进入老年代，而此时老年代也仍然没有内存容纳对象，将导致一次 Full GC]

2）伴随 concurrent mode failed，然后 Full GC：

[concurrent mode failed：CMS 回收速度慢，CMS 完成前，老年代已被占满，将导致一次 Full GC]

3）频繁 CMS GC：

[内存吃紧，老年代长时间处于较满的状态]

#### 2. 业务日志

业务日志除了关注系统异常与业务异常之外，还要关注服务执行耗时情况，耗时过长的服务调用如果没有熔断等机制，很容易导致应用性能下降或服务不可用，服务不可用很容易导致雪崩。

![图片](http://mmbiz.qpic.cn/mmbiz_png/0vU1ia3htaaMCzibBMVpstibWpOMeg5vzickQhIZ6wRDzAvmlibRicEBsu3OzKV4twm4tRAvTbmVYZ4Ms2oBBT9hUMhA/?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

上面是某一接口的调用情况，虽然大部分调用没有发生异常，但是执行耗时相对比较长。

> grep ‘[0-9]{3,}ms’ *.log
>
> 找出调用耗时大于 3 位数的 dao 方法，把 3 改成 4 就是大于 4 位数

互联网应用目前几乎采用分布式架构，但不限于服务框架、消息中间件、分布式缓存、分布式存储等等。

那么这些应用日志如何聚合起来进行分析呢 ?

首先，你需要一套分布式链路调用跟踪系统，通过在系统线程上线文间透传 traceId 和 rpcId，将所有日志进行聚合，例如淘宝的鹰眼，spring cloud zipkin 等等。

### 五、案列分析

#### CPU 使用率高问题定位

![图片](http://mmbiz.qpic.cn/mmbiz_png/0vU1ia3htaaMCzibBMVpstibWpOMeg5vzicky2QcvAPgOicrD4yUibDSMLveaTlkasam3JHnzia3CA70eLo0u2P9t06qA/?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

按照定位流程首先排除了系统层面的问题。

利用 top -Hp 6814 输出进程 ID 为 6814 的所有线程 CPU 使用率情况，发现某个线程使用率比较高，有些异常。

```
printf '%x\n' 2304     #输出线程 ID 的 16 进制
jstack pid | grep '0x900' -C 30 --color
```

输出的日志表明该线程一直处于与 mysql I/O 状态：

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/0vU1ia3htaaMCzibBMVpstibWpOMeg5vzicku4rEjTDNicLf4sbXLvuDGOAa5VPjMsibrpBA7v9PM4m4rfzBYt1jW35w/?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

利用 jmap -dump:format=b,file=/usr/local/logs/gc/dump.hprof {pid} 以二进制输出档当前内存的堆情况，然后可以导入 MAT 等工具进行分析。

如下图所示，点击 MAT 的支配树可以发现存在某个超大对象数组，实例对象数目多大 30 多万个。

![图片](http://mmbiz.qpic.cn/mmbiz_png/0vU1ia3htaaMCzibBMVpstibWpOMeg5vzickbQRxGAtxTrHTpJY1ib0vgrvWrhOvaSa0mMcs6NqYia8iaWDSzAvOqvZHQ/?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

经过分析发现数组中每一个对象都是核心业务对象，我们的业务系统有一个定时任务线程会访问数据库某张业务表的所有记录。

然后加载至内存然后进行处理因此内存吃紧，导致 CPU 突然飙升。发现该问题后，已对该方案进行重新设计。