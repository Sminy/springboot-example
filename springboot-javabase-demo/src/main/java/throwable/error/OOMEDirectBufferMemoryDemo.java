package throwable.error;


import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

/**
 * -Xms5m -Xmx5m -XX:+PrintGCDetails -XX:MaxDirectMemorySize=5m
 * java.lang.OutOfMemoryError: Direct buffer memory
 * 分析：但如果不断分配本地内存，堆内存很少使用，那么JV就不需要执行GC，DirectByteBuffer对象们就不会被回收，
 *      这时候堆内存充足，但本地内存可能已经使用光了，再次尝试分配本地内存就会出现OutOfMemoryError，那程序就直接崩溃了。
 */
public class OOMEDirectBufferMemoryDemo {
    public static void main(String[] args) throws InterruptedException {
//        System.out.println(String.format("配置的maxDirectMemory: %.2f MB", VM.maxDirectMemory() / 1024.0 /1024));
        TimeUnit.SECONDS.sleep(3);
        ByteBuffer allocate = ByteBuffer.allocate(40 * 1024 * 1024); // 分配堆内内存
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(40 * 1024 * 1024); // 分配堆外内存，本地直接内存
    }
}
