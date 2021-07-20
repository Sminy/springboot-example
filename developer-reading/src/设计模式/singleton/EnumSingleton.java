package 设计模式.singleton;

public class EnumSingleton {

    public static void main(String[] args) {
        Singleton.INSTANCE.say();
    }
}

enum Singleton {
    INSTANCE;
    public void say() {
        System.out.println("hahaha");
    }
}



