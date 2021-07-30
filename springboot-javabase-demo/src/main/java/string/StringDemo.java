package string;

/**
 *  字符串操作:
 *      1）直接使用双引号声明出来的String对象会直接存储在常量池中；
 *      2）String对象的intern方法会得到字符串对象在常量池中对应的引用，如果常量池中没有对应的字符串，则该字符串将被添加到常量池中，然后返回常量池中字符串的引用；
 *      3） 字符串的+操作其本质是创建了StringBuilder对象进行append操作，然后将拼接后的StringBuilder对象用toString方法处理成String对象，这一点可以用javap -c命令获得class文件对应的JVM字节码指令就可以看出来。
 *
 *    String/StringBuilder/StringBuffer
 *      1）String是不可变字符序列，StringBuilder和StringBuffer是可变字符序列。
 *      2）执行速度StringBuilder > StringBuffer > String。
 *      3）StringBuilder是非线程安全的，StringBuffer是线程安全的。
 */
public class StringDemo {
    public static void main(String[] args) {
        String s1 = "AB";
        String s2 = new String("AB");
        String s3 = "A";
        String s4 = "B";
        String s5 = "A" + "B";
        String s6 = s3 + s4;
        System.out.println(s1 == s2);  //false
        System.out.println(s1 == s5); // true
        System.out.println(s1 == s6); // false
        System.out.println(s1 == s6.intern()); // true
        System.out.println(s2 == s2.intern()); // false
    }
}
