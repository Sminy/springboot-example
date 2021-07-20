package 设计模式.singleton;

/**
 * 饿汉式（静态代码块）
 * 无线程安全问题，不能延迟加载，影响系统性能。
 */
public class HungrySingleton2 {

    private static HungrySingleton2 instance;

    static {
        instance = new HungrySingleton2();
    }

    private HungrySingleton2() {
    }

    public static HungrySingleton2 getInstance() {
        return instance;
    }
}
