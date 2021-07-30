package base.test;

/**
 *  有一个初始化的Java字符串（JDK出娘胎自带的），在加载sun.misc.Version这个类的时候进入常量池。
 */
public class StringInternDemo {

    public static void main(String[] args) {

        String str1 = new StringBuilder("58").append("tongcheng").toString();
        System.out.println(str1);
        System.out.println(str1.intern());
        System.out.println(str1 == str1.intern());

        System.out.println();

        String str2 = new StringBuilder("j").append("ava").toString();
        System.out.println(str2);
        System.out.println(str2.intern());
        System.out.println(str2 == str2.intern());  // false

    }

}