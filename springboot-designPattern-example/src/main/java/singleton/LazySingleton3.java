package singleton;

/**
 * 懒汉式3
 * 线程不安全，会产生多个实例，不可用。
 */
public class LazySingleton3 {

    private static LazySingleton3 instance;

    private LazySingleton3() {
    }

    public static synchronized LazySingleton3 getInstance() {
        if (instance != null) {
            synchronized (LazySingleton3.class) {
                instance = new LazySingleton3();
            }
        }
        return instance;
    }
}
