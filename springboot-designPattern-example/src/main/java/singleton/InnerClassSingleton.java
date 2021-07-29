package singleton;

/**
 *  静态内部类，线程安全，延迟加载，推荐使用
 */
public class InnerClassSingleton {

    private static class SingletonHolder {
        private static final InnerClassSingleton INSTANCE = new InnerClassSingleton();
    }

    private InnerClassSingleton() {
    }

    public InnerClassSingleton getInstance() {
        return SingletonHolder.INSTANCE;
    }
}
