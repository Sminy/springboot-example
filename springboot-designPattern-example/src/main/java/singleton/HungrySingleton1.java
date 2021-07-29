package singleton;

/**
 *  饿汉式（静态常量）
 *  无线程安全问题，不能延迟加载，影响系统性能。
 */
public class HungrySingleton1 {

    private static final HungrySingleton1 INSTANCE = new HungrySingleton1();

    private HungrySingleton1() {
    }

    public static HungrySingleton1 getInstance() {
        return INSTANCE;
    }
}
