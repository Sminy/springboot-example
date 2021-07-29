package throwable.error;

import java.util.ArrayList;
import java.util.List;

/**
 * -Xms10m -Xmx10m -XX:MaxDirectMemorySize=5m
 * java.lang.OutOfMemoryError: GC overhead limit exceeded
 *  分析：超过98%的时间用来做GC并且回收了不到2%的堆内存，连续多次GC 都只回收了不到2%的极端情况下才会抛出
 */
public class OOMEGCOverheadLimitExceededDemo {
    public static void main(String[] args) {
        int i = 0;
        List<Object> list = new ArrayList<>();
        try {
            while (true) {
                list.add(String.valueOf(i++).intern());
            }
        } catch (Exception e) {
            System.out.println("----------------- i ：" + i);
            e.printStackTrace();
            throw e;
        }
    }
}
