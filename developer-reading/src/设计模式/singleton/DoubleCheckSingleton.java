package 设计模式.singleton;

/**
 *   双重校验锁，线程安全，延迟加载，推荐使用。
 */
public class DoubleCheckSingleton {

    private static volatile DoubleCheckSingleton instance;

    private DoubleCheckSingleton() {
    }

    public DoubleCheckSingleton getInstance() {
        if (instance == null) {
            synchronized (DoubleCheckSingleton.class) {
                instance = new DoubleCheckSingleton();
            }
        }
        return instance;
    }
}
