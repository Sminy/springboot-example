package base.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *  多线程异常： java.util.ConcurrentModificationException
 *      解决方法之一：Vector
 *      解决方法之二：Collections.synchronizedList()
 *      解决方法之三：CopyOnWriteArrayList（推荐） 读写分离
 */
public class ArrayListNotSafeDemo {
    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        //List<String> list = new Vector<>();
        //List<String> list = Collections.synchronizedList(new ArrayList<>());
        //List<String> list = new CopyOnWriteArrayList<>();

        for (int i = 0; i < 30; i++) {
            new Thread(() -> {
                list.add(UUID.randomUUID().toString().substring(0, 8));
                System.out.println(list);
            }, String.valueOf(i)).start();
        }
    }
}
