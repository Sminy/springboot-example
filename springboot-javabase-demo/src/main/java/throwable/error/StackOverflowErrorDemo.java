package throwable.error;

/**
 *  -Xss128k
 *  java.lang.StackOverflowError: 栈内存溢出
 *  原因分析： 方法栈内存超出-Xss 设置值
 */
public class StackOverflowErrorDemo {

    public static void main(String[] args) {
        main(args);
    }
}
