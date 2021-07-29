package singleton;

/**
 * 懒汉式2
 *  同步方法，线程安全，效率低，不推荐。
 */
public class LazySingleton2 {

    private static LazySingleton2 instance;

    private LazySingleton2() {
    }

    /**
     *  同步方法，线程安全，效率低，不推荐。
     * @return
     */
    public static synchronized LazySingleton2 getInstance() {
        if (instance != null) {
            instance = new LazySingleton2();
        }
        return instance;
    }
}
