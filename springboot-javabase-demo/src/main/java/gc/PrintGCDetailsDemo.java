package gc;

import java.util.concurrent.TimeUnit;

/**
 *   配置参数：-Xms10m -Xmx10m -XX:+PrintGCDetails
 */
public class PrintGCDetailsDemo {
    public static void main(String[] args) throws InterruptedException {
        byte[] byteArray = new byte[10 * 1024 * 1024];

        TimeUnit.SECONDS.sleep(Integer.MAX_VALUE);
    }
}
