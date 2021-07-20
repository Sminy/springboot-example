## 线程池

### 0 阿里禁止使用Executors

阿里巴巴开发手册为什么禁止使用 `Executors` 去创建线程池，原因就是 `newFixedThreadPool()` 和 `newSingleThreadExecutor()`两个方法允许请求的最大队列长度是 `Integer.MAX_VALUE` ，可能会出现任务堆积，出现OOM。`newCachedThreadPool()`允许创建的线程数量为 `Integer.MAX_VALUE`，可能会创建大量的线程，导致发生OOM。它建议使用`ThreadPoolExecutor`方式去创建线程池，通过上面的分析我们也知道了其实`Executors` 三种创建线程池的方式最终就是通过`ThreadPoolExecutor`来创建的，只不过有些参数我们无法控制，如果通过`ThreadPoolExecutor`的构造器去创建，我们就可以根据实际需求控制线程池需要的任何参数，避免发生OOM异常。



```
Executors.newCachedThreadPool();        //创建一个缓冲池，缓冲池容量大小为Integer.MAX_VALUE
Executors.newSingleThreadExecutor();   //创建容量为1的缓冲池
Executors.newFixedThreadPool(int);    //创建固定容量大小的缓冲池
```



### 1 ThreadPoolExecutor构造函数

```
    public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
             Executors.defaultThreadFactory(), defaultHandler);
    }
     
```



```
    public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              ThreadFactory threadFactory,
                              RejectedExecutionHandler handler) {
        if (corePoolSize < 0 ||
            maximumPoolSize <= 0 ||
            maximumPoolSize < corePoolSize ||
            keepAliveTime < 0)
            throw new IllegalArgumentException();
        if (workQueue == null || threadFactory == null || handler == null)
            throw new NullPointerException();
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.workQueue = workQueue;
        this.keepAliveTime = unit.toNanos(keepAliveTime);
        this.threadFactory = threadFactory;
        this.handler = handler;
    }
```

**参数含义：**

**corePoolSize**：核心池的大小，这个参数跟后面讲述的线程池的实现原理有非常大的关系。在创建了线程池后，默认情况下，线程池中并没有任何线程，而是等待有任务到来才创建线程去执行任务，除非调用了prestartAllCoreThreads()或者prestartCoreThread()方法，从这2个方法的名字就可以看出，是预创建线程的意思，即在没有任务到来之前就创建corePoolSize个线程或者一个线程。默认情况下，在创建了线程池后，线程池中的线程数为0，当有任务来之后，就会创建一个线程去执行任务，当线程池中的线程数目达到corePoolSize后，就会把到达的任务放到缓存队列当中；

**maximumPoolSize**：线程池最大线程数，这个参数也是一个非常重要的参数，它表示在线程池中最多能创建多少个线程；

**keepAliveTime**：表示线程没有任务执行时最多保持多久时间会终止。默认情况下，只有当线程池中的线程数大于corePoolSize时，keepAliveTime才会起作用，直到线程池中的线程数不大于corePoolSize，即当线程池中的线程数大于corePoolSize时，如果一个线程空闲的时间达到keepAliveTime，则会终止，直到线程池中的线程数不超过corePoolSize。但是如果调用了allowCoreThreadTimeOut(boolean)方法，在线程池中的线程数不大于corePoolSize时，keepAliveTime参数也会起作用，直到线程池中的线程数为0；

**unit**：参数keepAliveTime的时间单位，有7种取值，在TimeUnit类中有7种静态属性：

**workQueue**：一个阻塞队列，用来存储等待执行的任务，这个参数的选择也很重要，会对线程池的运行过程产生重大影响，一般来说，这里的阻塞队列有以下几种选择：

**threadFactory**：用于设置创建线程的工厂，可以通过线程工厂给每个创建出来的线程做些更有意义的事情，比如设置daemon和优先级等等

**handler**：表示当拒绝处理任务时的策略，有以下四种取值：

```
1、AbortPolicy：直接抛出异常。
2、CallerRunsPolicy：只用调用者所在线程来运行任务。
3、DiscardOldestPolicy：丢弃队列里最近的一个任务，并执行当前任务。
4、DiscardPolicy：不处理，丢弃掉。
5、也可以根据应用场景需要来实现RejectedExecutionHandler接口自定义策略。如记录日志或持久化不能处理的任务。
```

### 2 newCachedThreadPool（）

最快，CPU 可能达到100%，造成服务器卡死

```
    public static ExecutorService newCachedThreadPool() {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new SynchronousQueue<Runnable>());
    }
```

### 3 newFixedThreadPool（int i）

较慢， OOM内存溢出

```
    public static ExecutorService newFixedThreadPool(int nThreads) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                                      0L, TimeUnit.MILLISECONDS,
                                      new LinkedBlockingQueue<Runnable>());
    }
```



#### 4 newSingleThreadScheduledExecutor()

最慢,   OOM内存溢出

```
    public static ExecutorService newSingleThreadExecutor() {
        return new FinalizableDelegatedExecutorService
            (new ThreadPoolExecutor(1, 1,
                                    0L, TimeUnit.MILLISECONDS,
                                    new LinkedBlockingQueue<Runnable>()));
    }
```



**提交优先级： 核心线程 > 工作队列 > 非核心线程**

**执行优先级： 核心线程 > 非核心线程 > 工作队列**

