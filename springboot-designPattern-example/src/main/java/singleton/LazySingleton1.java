package singleton;

/**
 * 懒汉式1
 *  线程不安全，不可用。
 */
public class LazySingleton1 {

    private static LazySingleton1 instance;

    private LazySingleton1() {
    }

    /**
     *  线程不安全，不可用。
     * @return
     */
    public static LazySingleton1 getInstance() {
        if (instance != null) {
            instance = new LazySingleton1();
        }
        return instance;
    }
}
