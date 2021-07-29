package throwable.error;

/**
 *  jvm参数： -Xms10m -Xmx10m
 *  原因：
 *      1、超出预期的访问量/数据量
 *      2、内存泄漏
 */
public class OOMEJavaHeapSpaceDemo {
    public static void main(String[] args) {
        byte[] bytes = new byte[80 * 1024 * 1024];
    }
}
