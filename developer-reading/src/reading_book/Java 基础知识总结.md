## Java基础知识总结

## 一 泛型

### 1.1 定义

​		在日常编程的过程中，泛型在这三个特性之中使用频率是最高的。”泛型”一词中的泛字可以理解为泛化的意思，即由具体的、个别的扩大为一般的。Oracle对泛型的官方定义是：泛型类型是通过类型参数化的泛型类或接口。一言以蔽之，泛型就是通过类型参数化，来解决程序的通用性设计和实现的若干问题。

泛型：泛化之义, 就是类型参数化来解决程序的通用性设计和实现若干问题。

### 1.2 作用

泛型是jdk1.5 后引入的，主要解决以下问题：

- 编译期类型检查
- 强制类型转换
- 可读性和灵活性

（1）编译期类型检查

![image-20210318173043201](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20210318173043201.png)

（2）强制类型转换

![image-20210318173156332](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20210318173156332.png)

（3）可读性和灵活性

泛型使用上的灵活性体现在很多方面，因为它本身实质上就是对于继承在使用上的一种增强。因为泛型在具体工作时，当编译器在编译源码的时候，首先要进行泛型类型参数的检查，检查出类型不匹配等问题，然后进行类型擦除并同时在类型参数出现的位置插入强制转换指令，从而实现泛型。

**简言之：编译器在编译源码时，首先进行泛型类型参数的检查，然后进行类型檫除并同时在类型参数出现的位置插入强制转换指令从而实现。**



（4）限定通配符和非限定通配符

- <? extends T> 上界通配符
- <? super T> 下界通配符
- <?> 无界通配符

## 二 注解

### 2.1 定义

Annotation（注解）：先看看官方给出的概念，注解是 Java 提供的一种对元程序中元素关联信息和元数据的途径和方法。

### 2.2 元注解

**元注解**是注解的注解，也就是对标签的描述。比如“木讷”、“帅气”只能用在人或动物身上，那么“只能用在人或动物身上”就是对“木讷”、“帅气”这两个标签的标签；恰好元注解中就有 @Target，表示修饰对象的范围，让我们详细看一下元注解都有哪些。



- @Target：表示修饰对象的范围，注解可以作用于 packages、class、interface、方法、成员变量、枚举、方法入参等等，@Target可以指明该注解可以修饰哪些内容。
- @Retention：时间长短，也就是注解在什么时间范围之内有效，比如在源码中有效，在 class 文件中有效，在 Runtime 运行时有效。
- @Documented：表示可以被文档化，比如可以被 javadoc 或类似的工具生成文档。
- @Inherited：表示这个注解会被继承，比如 @MyAnnotation 被 @Inherited 修饰，那么当 @MyAnnotation 作用于一个 class 时，这个 class 的子类也会被 @MyAnnotation 作用。

### 2.3 内置注解

Java 中最早内置了三种注解：

- @Override：检查该方法是否是重载方法；如果父类或实现的接口中，如果没有该方法，编译会报错。
- @Deprecated：已经过时的方法；如果使用该方法，会有警告提醒。
- @SuppressWarnings：忽略警告；比如使用了一个过时的方法会有警告提醒，可以为调用的方法增加 @SuppressWarnings 注解，这样编译器不在产生警告。

### 2.4 注解的使用场景

注解可以让编译器探测错误和警告；编译阶段可以利用注解生成文档、代码或做其他的处理；在代码运行阶段，一些注解还可以帮助完成代码提取之类的工作。

比如，使用过 Spring 框架的同学应该对 @Autowired 很熟悉了。使用 Spring 开发时，进行配置可以用 xml 配置文件的方式，现在用的更多的就是注解的方式。@Autowired 可以帮助我们注入一个定义好的 Bean。

@Autowired 的核心代码大概是这样的，作用就是 Spring 可以提取到使用 @Autowired 修饰的字段或方法做注入

```
private InjectionMetadata buildAutowiringMetadata(final Class<?> clazz) {
		List<InjectionMetadata.InjectedElement> elements = new ArrayList<>();
		Class<?> targetClass = clazz;

		do {
			final List<InjectionMetadata.InjectedElement> currElements = new ArrayList<>();

			ReflectionUtils.doWithLocalFields(targetClass, field -> {
				AnnotationAttributes ann = findAutowiredAnnotation(field);
				if (ann != null) {
					if (Modifier.isStatic(field.getModifiers())) {
						if (logger.isInfoEnabled()) {
							logger.info("Autowired annotation is not supported on static fields: " + field);
						}
						return;
					}
					boolean required = determineRequiredStatus(ann);
					currElements.add(new AutowiredFieldElement(field, required));
				}
			});

			ReflectionUtils.doWithLocalMethods(targetClass, method -> {
				Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
				if (!BridgeMethodResolver.isVisibilityBridgeMethodPair(method, bridgedMethod)) {
					return;
				}
				AnnotationAttributes ann = findAutowiredAnnotation(bridgedMethod);
				if (ann != null && method.equals(ClassUtils.getMostSpecificMethod(method, clazz))) {
					if (Modifier.isStatic(method.getModifiers())) {
						if (logger.isInfoEnabled()) {
							logger.info("Autowired annotation is not supported on static methods: " + method);
						}
						return;
					}
					if (method.getParameterCount() == 0) {
						if (logger.isInfoEnabled()) {
							logger.info("Autowired annotation should only be used on methods with parameters: " +
									method);
						}
					}
					boolean required = determineRequiredStatus(ann);
					PropertyDescriptor pd = BeanUtils.findPropertyForMethod(bridgedMethod, clazz);
					currElements.add(new AutowiredMethodElement(method, required, pd));
				}
			});

			elements.addAll(0, currElements);
			targetClass = targetClass.getSuperclass();
		}
		while (targetClass != null && targetClass != Object.class);

		return new InjectionMetadata(clazz, elements);
	}
```



## 三 反射

### 3.1 定义

​		JAVA反射机制是在**运行状态**中，对于任意一个类，都能够知道这个类的所有属性和方法；对于任意一个对象，都能够调用它的任意方法和属性；这种动态获取信息以及动态调用对象方法的功能称为java语言的反射机制。

通过反射，java可以动态的加载未知的外部配置对象，临时生成字节码进行加载使用，使代码更灵活，极大地提高应用的扩展性。

### 3.2  原理

![image-20210319093858068](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20210319093858068.png)

​		Java 程序启动后, 首先把 .java 源码编译成 .class 字节码, 然后再把 .class 字节码加载到 JVM 中运行. 当我们 new 一个对象时, Java 在代码编译期就已经知道对象类型了, 然后在运行期创建该对象. 而 Java 反射机制是在运行期反向地创建对象, 获取属性和方法, 



### 3.3 应用

demo1: spring 中注册bean对象

```
<!--定义bean-->
<beans
    <bean id="userId" class="com.tyshawn.domain.User"></bean>
</beans>

//这里读取xml配置文件, 获取到"com.tyshawn.domain.User", 然后通过反射创建对象
User user = (User) context.getBean("userId");
```



demo2:  spring配置jdbc数据源

```
spring:
    driverClassName: com.mysql.cj.jdbc.Driver
    type: com.zaxxer.hikari.HikariDataSource
```



**获取Class 类**

(1) Class.forName()

```
Class<?> clazz = Class.forName("com.tyshawn.domain.User");
```

(2) 类.class

```
Class<User> clazz = User.class;
```

(3) Object.getClass()

```
User user = new User();
Class<? extends User> clazz = user.getClass();
```

### 3.4 实例

(1) 调用 Class 对象的 newInstance()方法

```
Class<?> clazz = Class.forName("com.tyshawn.domain.User");
User user = (User) clazz.newInstance();  //只有无参构造
```

(2) 调用构造对象的 newInstance()方法

```
Class<?> clazz = Class.forName("com.tyshawn.domain.User");
//无参构造
Constructor<?> constructor = clazz.getConstructor();
User user = (User) constructor.newInstance();

//有参构造
Constructor<?> constructor1 = clazz.getConstructor(String.class, Integer.class);
User user1 = (User) constructor1.newInstance("tyshawn", 23);
```

（3）获取公有成员变量

```
Class<?> clazz = Class.forName("com.tyshawn.domain.User");
User user = (User) clazz.newInstance();

//获取共有成员变量
Field name = clazz.getField("name");
//赋值
name.set(user, "tyshaw");

System.out.println(user);
```

（4）获取私有成员变量

```

Class<?> clazz = Class.forName("com.tyshawn.domain.User");
User user = (User) clazz.newInstance();

//获取共有成员变量
Field age = clazz.getDeclaredField("age");
//去除私有权限
age.setAccessible(true);
//赋值
age.set(user, 23);

System.out.println(user);
```

（5）获取公有成员方法

```
Class<?> clazz = Class.forName("com.tyshawn.domain.User");
Constructor<?> constructor = clazz.getConstructor(String.class, Integer.class);
User user = (User) constructor.newInstance("Tyshawn", 23);

//获取无参成员方法
Method say = clazz.getMethod("say");
say.invoke(user); // Hello Tyshawn

//获取有参成员方法
Method say1 = clazz.getMethod("say", String.class);
say1.invoke(user, "Tom"); //Hello Tom
```

（6）获取私有成员方法

```
Class<?> clazz = Class.forName("com.tyshawn.domain.User");
Constructor<?> constructor = clazz.getConstructor(String.class, Integer.class);
User user = (User) constructor.newInstance("Tyshawn", 23);

//获取无参成员方法
Method method = clazz.getDeclaredMethod("say");
//去除私有权限
method.setAccessible(true);
method.invoke(user); // Hello Tyshawn

//获取有参成员方法
Method method1 = clazz.getDeclaredMethod("say", String.class);
//去除私有权限
method1.setAccessible(true);
method1.invoke(user, "Tom"); //Hello Tom
```

## 四 类加载

### 4.1 类加载过程

![image-20210319134248605](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20210319134248605.png)

多个java文件经过编译打包生成可运行jar包，最终由java命令运行某个主类的main函数启动程序，这里首先需要通过类加载器把主类加载到JVM，主类在运行过程中如果使用到其它类，会**逐步加载**（动态加载）这些类。

一个java文件从被加载到被卸载这个生命过程，总共要经历4个阶段：

加载->链接（验证+准备+解析）->初始化（使用前的准备）->使用->卸载

```
（1）加载：在硬盘上查找并通过IO读入字节码文件，将class字节码文件加载到内存中，并将这些数据转换成方法区中的运行时数据（静态变量、静态代码块、常量池等），在堆中生成一个Class类对象代表这个类（反射原理），作为方法区类数据的访问入口。

注意：使用到类时才会加载，例如调用类的main()方法，new对象等。

（2）验证：验证被加载后的类是否有正确的结构，类数据是否会符合虚拟机的要求，确保不会危害虚拟机安全（验证字节码的正确性）。

（3）准备：为类的静态变量（static filed）在方法区分配内存，并赋默认初值（0值或null值）。如static int a = 100 ;静态变量a就会在准备阶段被赋默认值0。

对于一般的成员变量是在类实例化时候，随对象一起分配在堆内存中。

另外，静态常量（static final filed）会在准备阶段赋程序设定的初值，如static final int a = 666;  静态常量a就会在准备阶段被直接赋值为666，对于静态变量，这个操作是在初始化阶段进行的。

（4）解析：将符号引用替换为直接引用，该阶段会把一些静态方法（符号引用，比如main()方法）替换为指向数据所存内存的指针或句柄等(直接引用)，这是所谓的静态链接过程(类加载期间完成)，动态链接是在程序运行期间完成的将符号引用替换为直接引用（将类的二进制数据中的符号引用换为直接引用）。

（5）初始化：类的初始化的主要工作是为静态变量赋程序设定的初值。
	如static int a = 100;在准备阶段，a被赋默认值0，在初始化阶段就会被赋值为100。	
```



### 4.2 Java 程序初始化顺序

1. 父类的静态变量
2. 父类的静态代码块
3. 子类的静态变量
4. 子类的静态代码块
5. 父类的非静态变量
6. 父类的非静态代码块
7. 父类的构造方法
8. 子类的非静态变量
9. 子类的非静态代码块
10. 子类的构造方法



Demo:

```java

public class Demo01 {
    public static void main(String[] args) {
        B b = new B();
    }
}

class A{

    static String str1 = "父类A的静态变量";
    String str2 = "父类A的非静态变量";

    static {
        System.out.println("执行了父类A的静态代码块");
    }

    {
        System.out.println("执行了父类A的非静态代码块");
    }

    public A(){
        System.out.println("执行了父类A的构造方法");
    }
}

class B extends A{

    static String str1 = "子类B的静态变量";
    String str2 = "子类B的非静态变量";

    static {
        System.out.println("执行了子类B的静态代码块");
    }

    {
        System.out.println("执行了子类B的非静态代码块");
    }

    public B(){
        System.out.println("执行了子类B的构造方法");
    }
}
```

![img](https://upload-images.jianshu.io/upload_images/14265221-e263e5964a6a649f.png?imageMogr2/auto-orient/strip|imageView2/2/w/439/format/webp)



###  4.3 类的引用

#### （1）主动引用（一定会初始化）

- new一个类的对象。
- 调用类的静态成员(除了final常量)和静态方法。
- 使用java.lang.reflect包的方法对类进行反射调用。
- 当虚拟机启动，java Hello，则一定会初始化Hello类。说白了就是先启动main方法所在的类。
- 当初始化一个类，如果其父类没有被初始化，则先会初始化他的父类

#### （2）被动引用

- 当访问一个静态域时，只有真正声明这个域的类才会被初始化。例如：通过子类引用父类的静态变量，不会导致子类初始化。
- 通过数组定义类引用，不会触发此类的初始化。
- 引用常量不会触发此类的初始化（常量在编译阶段就存入调用类的常量池中了）。

```
public class Demo02 {
    public static void main(String[] args) throws ClassNotFoundException {
        //主动引用：new一个类的对象
//        People people = new People();
        //主动引用：调用类的静态成员(除了final常量)和静态方法
//        People.getAge();
//        System.out.println(People.age);
        //主动调用：使用java.lang.reflect包的方法对类进行反射调用
//        Class.forName("pri.xiaowd.classloader.People");


        //被动引用:当访问一个静态域时，只有真正声明这个域的类才会被初始化。
//        System.out.println(WhitePeople.age);
        //被动引用:通过数组定义引用，不会初始化
//        People[] people = new People[10];
        //被动引用:引用常量不会触发此类的初始化
        System.out.println(People.num);
    }
    //主动调用：先启动main方法所在的类
//    static {
//        System.out.println("main方法所在的类在虚拟机启动时就加载");
//    }
}

class People{

    static int age = 3;
    static final int num = 20;

    static {
        System.out.println("People被初始化了！");
    }

    public People() {
    }

    public static int getAge() {
        return age;
    }

    public static void setAge(int age) {
        People.age = age;
    }
}

class WhitePeople extends People{

    static {
        System.out.println("WhitePeople被初始化了！");
    }
}
```

### 4.4 类加载器的原理

#### （1）类缓存

​		标准的Java SE类加载器可以按要求查找类，一旦某个类被加载到类加载器中，它将维持加载（缓存）一段时间。不过，JVM垃圾收集器可以回收这些Class对象。

#### （2）类加载器分类

![img](https://upload-images.jianshu.io/upload_images/14265221-549d6e3ca7ab0b5a.png?imageMogr2/auto-orient/strip|imageView2/2/w/376/format/webp)



**引导类加载器（bootstrap class loader）**
 （1）它用来加载 Java 的核心库(JAVA_HOME/jre/lib/rt.jar,sun.boot.class.path路径下的内容)，是用原生代码（C语言）来实现的，并不继承自 java.lang.ClassLoader。
 （2）加载扩展类和应用程序类加载器。并指定他们的父类加载器。

**扩展类加载器（extensions class loader）**
 （1）用来加载 Java 的扩展库(JAVA_HOME/jre/ext/*.jar，或java.ext.dirs路径下的内容) 。Java 虚拟机的实现会提供一个扩展库目录。该类加载器在此目录里面查找并加载 Java类。
 （2）由sun.misc.Launcher$ExtClassLoader实现。

**应用程序类加载器（application class loader）**
 （1）它根据 Java 应用的类路径（classpath，java.class.path 路径下的内容）来加载 Java 类。**一般来说，Java 应用的类都是由它来完成加载的。**
 （2）由sun.misc.Launcher$AppClassLoader实现。

**自定义类加载器**
 （1）开发人员可以通过继承 java.lang.ClassLoader类的方式实现自己的类加载器，以满足一些特殊的需求。

#### （3）java.class.ClassLoader类

**（1）作用：**

- java.lang.ClassLoader类的基本职责就是根据一个指定的类的名称，找到或者生成其对应的字节代码，然后从这些字节代码中定义出一个Java类，即java.lang.Class类的一个实例。

ClassLoader还负责加载 Java 应用所需的资源，如图像文件和配置文件等。
 **（2）常用方法：**

- getParent() 返回该类加载器的父类加载器。
- loadClass(String name) 加载名称为 name的类，返回的结果是java.lang.Class类的实例。
   **此方法负责加载指定名字的类，首先会从已加载的类中去寻找，如果没有找到；从parent ClassLoader[ExtClassLoader]中加载；如果没有加载到，则从Bootstrap ClassLoader中尝试加载(findBootstrapClassOrNull方法), 如果还是加载失败，则自己加载。如果还不能加载，则抛出异常ClassNotFoundException。**
- findClass(String name) 查找名称为 name的类，返回的结果是java.lang.Class类的实例。
- findLoadedClass(String name) 查找名称为 name的已经被加载过的类，返回的结果是 java.lang.Class类的实例。
- defineClass(String name, byte[] b, int off, int len) 把字节数组 b中的内容转换成 Java 类，返回的结果是java.lang.Class类的实例。这个方法被声明为 final的。
- resolveClass(Class<?> c) 链接指定的 Java 类。

###  4.5 类加载器的代理模式

​	代理模式即是将指定类的加载交给其他的类加载器。常用双亲委托机制。

#### （1）双亲委托机制

​		某个特定的类加载器接收到类加载的请求时，会将加载任务委托给自己的父类，直到最高级父类**引导类加载器（bootstrap class loader）**，如果父类能够加载就加载，不能加载则返回到子类进行加载。如果都不能加载则报错。**ClassNotFoundException

![img](https://upload-images.jianshu.io/upload_images/14265221-3d7d7649e882f1c2.png?imageMogr2/auto-orient/strip|imageView2/2/w/371/format/webp)



双亲委托机制是为了保证 Java 核心库的类型安全。这种机制保证不会出现用户自己能定义java.lang.Object类等的情况。例如，用户定义了java.lang.String，那么加载这个类时最高级父类会首先加载，发现核心类中也有这个类，那么就加载了核心类库，而自定义的永远都不会加载。

值得注意是，双亲委托机制是代理模式的一种，但并不是所有的类加载器都采用双亲委托机制。在tomcat服务器类加载器也使用代理模式，所不同的是它是首先尝试去加载某个类，如果找不到再代理给父类加载器。这与一般类加载器的顺序是相反的。

### 4.6 自定义类加载器

#### 自定义类加载器的流程

 （1）首先检查请求的类型是否已经被这个类装载器装载到命名空间中了，如果已经装载，直接返回；否则转入步骤2。
 （2）委派类加载请求给父类加载器，如果父类加载器能够完成，则返回父类加载器加载的Class实例；否则转入步骤3。
 （3）调用本类加载器的findClass（…）方法，试图获取对应的字节码，如果获取的到，则调用defineClass（…）导入类型到方法区；如果获取不到对应的字节码或者其他原因失败，返回异常给loadClass（…）， loadClass（…）转抛异常，终止加载过程（注意：这里的异常种类不止一种）。

**注意：被两个类加载器加载的同一个类，JVM认为是不相同的类。**



```java
import java.io.*;

/**
 * @ClassName FileSystemClassLoader
 * @Description 自定义文件类加载器
 */
public class FileSystemClassLoader extends ClassLoader {
    private String rootDir;//根目录

    public FileSystemClassLoader(String rootDir) {
        this.rootDir = rootDir;
    }

    /**
     * @MethodName findClass
     * @Descrition 加载类
     * @Param [name]
     * @return java.lang.Class<?>
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> loadedClass = findLoadedClass(name);//查询该类是否已经被加载过
        if(loadedClass != null){  //该类已经被加载过了,直接返回
            return loadedClass;
        }else{  //该类还没有被加载过
            ClassLoader classLoader = this.getParent();//委派给父类加载
            try {
                loadedClass = classLoader.loadClass(name);
            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
            }
            if(loadedClass != null){  //父类加载成功,返回
                return loadedClass;
            }else{
                byte[] classData = getClassData(name);
                if(classData == null){
                    throw new ClassNotFoundException();
                }else{
                    loadedClass = defineClass(getName(),classData,0,classData.length);
                }
            }
        }
        return loadedClass;
    }

    /**
     * @MethodName getClassData
     * @Descrition 根据类名获得对应的字节数组
     * @Param [name]
     * @return byte[]
     */
    private byte[] getClassData(String name) {
        //pri.xiaowd.test.A  -->  D:/myjava/pei/xiaowd/test/A.class
        String path = rootDir + "/" + name.replace('.','/') + ".class";
//        System.out.println(path);
        InputStream is = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            is = new FileInputStream(path);

            byte[] bytes = new byte[1024];
            int temp = 0;
            while((temp = is.read(bytes)) != -1){
                baos.write(bytes,0,temp);
            }
            return baos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if(baos != null){
                    baos.close();
                }
                if(is != null){
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
```



### 4.7 线程上下文类加载器	

通常当你需要动态加载资源的时候 , 你至少有三个 ClassLoader 可以选择 :
 1.系统类加载器或叫作应用类加载器 (system classloader or application classloader)
 2.当前类加载器
 3.当前线程类加载器

**• 当前线程类加载器是为了抛弃双亲委派加载链模式。**
 		每个线程都有一个关联的上下文类加载器。如果你使用new Thread()方式生成新的线程，新线程将继承其父线程的上下文类加载器。如果程序对线程上下文类加载器没有任何改动的话，程序中所有的线程将都使用系统类加载器作为上
 下文类加载器。
 **• Thread.currentThread().getContextClassLoader()**



### 4.8 Tomcat服务器的类加载器

​		每个 Web 应用都有一个对应的类加载器实例。该类加载器也使用代理模式(不同于前面说的双亲委托机制)，所不同的是它是首先尝试去加载某个类，如果找不到再代理给父类加载器。这与一般类加载器的顺序是相反的。但也是为了保证安全，这样核心库就不在查询范围之内。



## 五 字符串常量

面试题**:** 相信绝对是个经典兼考倒一堆人的题目

**1. String a = new String("1"+"2")共建了几个对象？**

​	答：**2个**

第一个：看构造器里面（"1"+"2"）,这个是在编译期就已经做了处理，即代表生成一个字符串："12"

第二个：当使用new的方法创建字符串时，注意这个”new“，就**表示直接开辟了内存空间**；然后**把值"引用”的常量池中的“12”**。最后返回该对象引用。

2. **同理：String str="a"+"b"+"c" 共建了几个对象？**

   答：**只有1个**

   在编译期已经常量折叠为"abc", 通过**编译器优化**后，得到的效果是 String **str**="abc"

3. **String str2 = new String("ABC") + "ABC" ; 会创建多少个对象?**

答：3个；new String("ABC") ：常量池+堆**各一个**，2个；后面再 "+ ABC": **常量池**新生成 “ABCABC" 1个；



## 六 序列化

**什么是Java序列化？**

**序列化**：Java中的序列化机制能够将一个实例对象信息写入到一个字节流中（**只序列化对象的属性值，而不会去序列化方法**），序列化后的对象可用于网络传输，或者持久化到数据库、磁盘中。

**反序列化**：需要对象的时候，再通过字节流中的信息来重构一个相同的对象。

实现接口：

```java
public class User implements Serializable {

    private static final long serialVersionUID = 1L;
}
```

String 内部实现了Serializable接口， 序列化对象时如果希望哪个属性不被序列化，则用`transient`关键字修饰即可

因为序列化对象时，如果不显示的设置`serialVersionUID`，Java在序列化时会根据对象属性自动的生成一个`serialVersionUID`，再进行存储或用作网络传输。

在反序列化时，会根据对象属性自动再生成一个新的`serialVersionUID`，和序列化时生成的`serialVersionUID`进行比对，两个`serialVersionUID`相同则反序列化成功，否则就会抛异常，即是InvalidCastException。



## 七 单例模式几种写法

#### 7.1 双重锁写法

```java
class Singleton {
    private static volatile Singleton instance;   //volatile保证线程间的可见性

    private Singleton() {
    }

    //提供一个静态的公有方法，加入双重检查代码，解决线程安全问题，同时解决懒加载问题
    //同时保证了效率，推荐使用
    public static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}
```



#### 7.2 饿汉式（静态常量）

```java
class Singleton{
    //1.构造器私有化，外部不能new
    private Singleton(){

    }

    //2.本类内部创建对象实例
    private final static Singleton instance = new Singleton();

    //3.提供一个公有的静态方法，返回实例对象
    public static Singleton getInstance(){
        return instance;
    }
}
```

**优点：**这种写法比较简单，就是在类装载的时候就完成实例化，避免了线程同步问题。 

**缺点：**在类装载的时候就完成实例化，没有达到Lazy Loading的效果。如果从始至终从未使用过这个实例，则会造成内存的浪费。 



#### 7.3 饿汉式(静态代码块)

```java
class Singleton {
    //1.构造器私有化，外部不能new
    private Singleton() {

    }

    //2.本类内部创建对象实例
    private static Singleton instance;

    static {  //在静态代码块中，创建单例对象
        instance = new Singleton();
    }

    //3.提供一个公有的静态方法，返回实例对象
    public static Singleton getInstance() {
        return instance;
    }
}

```

#### 7.4 枚举（推荐）

```java
enum Singleton{
    INSTANCE; //属性
    public void doSomething(){
        System.out.println("do something");
    }
}

```



## 八 多线程

#### 8.1 竞态条件

**竞态条件（Race Condition）**：计算的正确性取决于多个线程的交替执行时序时，就会发生竞态条件。



#### 8.2 fail-fast & fail-safe

**快速失败（fail-fast）**: 在使用迭代器对集合对象进行遍历的时候，如果 A 线程正在对集合进行遍历，此时 B 线程对集合进行修改（增加、删除、修改），或者 A 线程在遍历过程中对集合进行修改，都会导致 A 线程抛出 ConcurrentModificationException 异常。

```java
HashMap hashMap = new HashMap();
hashMap.put("不只Java-1", 1);
hashMap.put("不只Java-2", 2);
hashMap.put("不只Java-3", 3);

Set set = hashMap.entrySet();
Iterator iterator = set.iterator();
while (iterator.hasNext()) {   // 出现ConcurrentModificationException 异常
       System.out.println(iterator.next());
    hashMap.put("下次循环会抛异常", 4);
    System.out.println("此时 hashMap 长度为" + hashMap.size());
}
```

**为什么在用迭代器遍历时，修改集合就会抛异常时？**

原因是迭代器在遍历时直接访问集合中的内容，并且在遍历过程中使用一个 modCount 变量。集合在被遍历期间如果内容发生变化，就会改变 modCount 的值。

每当迭代器使用 hashNext()/next() 遍历下一个元素之前，都会检测 modCount 变量是否为 expectedModCount 值，是的话就返回遍历；否则抛出异常，终止遍历。



**安全失败（fail-safe）**:  

采用安全失败机制的集合容器，在遍历时不是直接在集合内容上访问的，而是先复制原有集合内容，在拷贝的集合上进行遍历。

由于迭代时是对原集合的拷贝进行遍历，所以在遍历过程中对原集合所作的修改并不能被迭代器检测到，故不会抛 ConcurrentModificationException 异常

```java
ConcurrentHashMap concurrentHashMap = new ConcurrentHashMap();
concurrentHashMap.put("不只Java-1", 1);
concurrentHashMap.put("不只Java-2", 2);
concurrentHashMap.put("不只Java-3", 3);

Set set = concurrentHashMap.entrySet();
Iterator iterator = set.iterator();

while (iterator.hasNext()) {
    System.out.println(iterator.next());
    concurrentHashMap.put("下次循环正常执行", 4);
}
System.out.println("程序结束");
```



## 九  零拷贝

从WIKI的定义中，我们看到“零拷贝”是指计算机操作的过程中，CPU不需要为数据在内存之间的拷贝消耗资源。而它通常是指计算机在网络上发送文件时，不需要将文件内容拷贝到用户空间（User Space）而直接在内核空间（Kernel Space）中传输到网络的方式。

#### 零拷贝给我们带来的好处

- 减少甚至完全避免不必要的CPU拷贝，从而让CPU解脱出来去执行其他的任务
- 减少内存带宽的占用
- 通常零拷贝技术还能够减少用户空间和操作系统内核空间之间的上下文切换

#### 零拷贝的实现

零拷贝实际的实现并没有真正的标准，取决于操作系统如何实现这一点。零拷贝完全依赖于操作系统。操作系统支持，就有；不支持，就没有。不依赖Java本身。

#### 传统I/O

在Java中，我们可以通过InputStream从源数据中读取数据流到一个缓冲区里，然后再将它们输入到OutputStream里。我们知道，这种IO方式传输效率是比较低的。那么，当使用上面的代码时操作系统会发生什么情况：



![img](https://upload-images.jianshu.io/upload_images/12038882-10d75804b44e2bed.jpg?imageMogr2/auto-orient/strip|imageView2/2/w/631/format/webp)

​	

代码：IO文件传输

```
Socket socket = new Socket(HOST, PORT);
InputStream inputStream = new FileInputStream(FILE_PATH);
OutputStream outputStream = new DataOutputStream(socket.getOutputStream());

byte[] buffer = new byte[4096];
while (inputStream.read(buffer) >= 0) {
    outputStream.write(buffer);
}

outputStream.close();
socket.close();
inputStream.close();
```

​																					**传统IO**



![图片](https://mmbiz.qpic.cn/mmbiz_png/UdK9ByfMT2MyvPMdQIsiaX5OgicHGibjJPvicxYx86H6YJv0jkhks69YEAYicKmvibd3sELgyazUhs6eoBlL6OmE0m9g/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



描述：

1. JVM向OS发出read()系统调用，触发上下文切换，从用户态切换到内核态。
2. 从外部存储（如硬盘）读取文件内容，通过直接内存访问（DMA）存入内核地址空间的缓冲区。
3. 将数据从内核缓冲区拷贝到用户空间缓冲区，read()系统调用返回，并从内核态切换回用户态。
4. JVM向OS发出write()系统调用，触发上下文切换，从用户态切换到内核态。
5. 将数据从用户缓冲区拷贝到内核中与目的地Socket关联的缓冲区。
6. 数据最终经由Socket通过DMA传送到硬件（如网卡）缓冲区，write()系统调用返回，并从内核态切换回用户态。



简化图：

![图片](https://mmbiz.qpic.cn/mmbiz_png/UdK9ByfMT2MyvPMdQIsiaX5OgicHGibjJPvgFiakaU39a3JY8YMLOia7lbELXALbN3DBJcVrM9MqeSRvGwG7FTalV5w/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

传统方法的上下文切换过程

我们都知道，上下文切换是CPU密集型的工作，数据拷贝是I/O密集型的工作。如果一次简单的传输就要像上面这样复杂的话，效率是相当低下的。零拷贝机制的终极目标，就是消除冗余的上下文切换和数据拷贝，提高效率。

#### 零拷贝：

通过上面的分析可以看出，第2、3次拷贝（也就是从内核空间到用户空间的来回复制）是没有意义的，数据应该可以直接从内核缓冲区直接送入Socket缓冲区。零拷贝机制就实现了这一点。不过零拷贝需要由操作系统直接支持，不同OS有不同的实现方法。大多数Unix-like系统都是提供了一个名为sendfile()的系统调用，在其man page中，就有这样的描述：

下面是零拷贝机制下，数据传输的时序图。

![图片](https://mmbiz.qpic.cn/mmbiz_png/UdK9ByfMT2MyvPMdQIsiaX5OgicHGibjJPvPjISaqXnZPnyo507gjItyNN8BJTYbicp57y8WWgMwM3icXwgshWdZIPQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

零拷贝方法的时序图

可见确实是消除了从内核空间到用户空间的来回复制，因此“zero-copy”这个词实际上是站在内核的角度来说的，并不是完全不会发生任何拷贝。

在Java NIO包中提供了零拷贝机制对应的API，即FileChannel.transferTo()方法。不过FileChannel类是抽象类，transferTo()也是一个抽象方法，因此还要依赖于具体实现。FileChannel的实现类并不在JDK本身，而位于sun.nio.ch.FileChannelImpl类中，零拷贝的具体实现自然也都是native方法，看官如有兴趣可以自行查找源码来看，这里不再赘述。

将传统方式的发送端逻辑改写一下，大致如下。

```java
SocketAddress socketAddress = new InetSocketAddress(HOST, PORT);
SocketChannel socketChannel = SocketChannel.open();
socketChannel.connect(socketAddress);

File file = new File(FILE_PATH);
FileChannel fileChannel = new FileInputStream(file).getChannel();
fileChannel.transferTo(0, file.length(), socketChannel);

fileChannel.close();
socketChannel.close();
```

![图片](https://mmbiz.qpic.cn/mmbiz_png/UdK9ByfMT2MyvPMdQIsiaX5OgicHGibjJPvLL8rzelu4CQ2QSrKkqA05GIlxfv4UqnRWicKbFkicTGSm1pgqLKK8nqw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

#### 应用

- 很多框架，基于Netty的实现
- 消息中间件Kafka 



#### Linux 零拷贝方案

传统的I/O操作读取文件并通过Socket发送，需要经过4次上下文切换、2次CPU数据拷贝和2次DMA控制器数据拷贝，如下图：

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/XVbUB37bCmAvhcFOPs261qz7h5fWibMVd2mF7X0L0wlcTyiaP17Pia89Wz7wxPc9qx8iap5DCwaFibFm0esFySoOjKw/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

从中也可以看得出提高性能可以从减少数据拷贝和上下文切换的次数着手，在Linux操作系统层面上有4种实现方案：内存映射**mmap、sendfile、splice、tee**，这些实现中或多多少的减少数据拷贝次数或减少上下文切换次数。

操作系统层面的减少数据拷贝次数主要是指用户空间和内核空间的数据拷贝，因为只有他们的拷贝是大量消耗CPU时间片的，而DMA控制器拷贝数据CPU参与的工作较少，只是辅助作用。

现实中对零拷贝的概念有广义和狭义之分，广义上是指只要减少了数据拷贝的次数就称之为零拷贝；狭义上是指真正的零拷贝，比如上例中避免2和3的CPU拷贝。



##### 什么是零拷贝技术（zero-copy）？

零拷贝主要的任务就是避免CPU将数据从一块存储拷贝到另外一块存储，主要就是利用各种零拷贝技术，避免让CPU做大量的数据拷贝任务，减少不必要的拷贝，或者让别的组件来做这一类简单的数据传输任务，让CPU解脱出来专注于别的任务。这样就可以让系统资源的利用更加有效。

我们继续回到引文中的例子，我们如何减少数据拷贝的次数呢？一个很明显的着力点就是减少数据在内核空间和用户空间来回拷贝，这也引入了零拷贝的一个类型：

**让数据传输不需要经过 user space。**



下面我们逐一看看他们的设计思想和实现方案：

##### （1）mmap

![图片](https://mmbiz.qpic.cn/mmbiz_png/1TDxR6xkRSFlHbtX4vP30w4hPqQFF2QTeIofPNS8lkSNM0BgGRuxvZgsvia7IwYDeYegDqtW4U1B931alfVl78Q/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



mmap内存映射

既然是内存映射，首先来了解解下虚拟内存和物理内存的映射关系，虚拟内存是操作系统为了方便操作而对物理内存做的抽象，他们之间是靠页表(Page Table)进行关联的，关系如下

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/XVbUB37bCmAvhcFOPs261qz7h5fWibMVdo9nU5j6zCqVMXdkyiak3x5zNiav35iauw80BiblyUMdYy8skiaIqgiczibIcA/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

每个进程都有自己的PageTable，进程的虚拟内存地址通过PageTable对应于物理内存，内存分配具有惰性，它的过程一般是这样的：进程创建后新建与进程对应的PageTable，当进程需要内存时会通过PageTable寻找物理内存，如果没有找到对应的页帧就会发生缺页中断，从而创建PageTable与物理内存的对应关系。虚拟内存不仅可以对物理内存进行扩展，还可以更方便地灵活分配，并对编程提供更友好的操作。

内存映射(mmap)是指用户空间和内核空间的虚拟内存地址同时映射到同一块物理内存，用户态进程可以直接操作物理内存，避免用户空间和内核空间之间的数据拷贝。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/XVbUB37bCmAvhcFOPs261qz7h5fWibMVdADT25kcH4e5TTvjtRU78Ozx2kYcVwY2un6DuqNS9biaITfk6ZYkOedQ/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

它的具体执行流程是这样的

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/XVbUB37bCmAvhcFOPs261qz7h5fWibMVd1g3ics2X68l9nxcpkvnreVn7aAsRDffHvC56aPwvwGV53WvlzhIibpuQ/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

1. 用户进程通过系统调用mmap函数进入内核态，发生第1次上下文切换，并建立内核缓冲区；
2. 发生缺页中断，CPU通知DMA读取数据；
3. DMA拷贝数据到物理内存，并建立内核缓冲区和物理内存的映射关系；
4. 建立用户空间的进程缓冲区和同一块物理内存的映射关系，由内核态转变为用户态，发生第2次上下文切换；
5. 用户进程进行逻辑处理后，通过系统调用Socket send，用户态进入内核态，发生第3次上下文切换；
6. 系统调用Send创建网络缓冲区，并拷贝内核读缓冲区数据；
7. DMA控制器将网络缓冲区的数据发送网卡，并返回，由内核态进入用户态，发生第4次上下文切换；

**总结**

1. 避免了内核空间和用户空间的2次CPU拷贝，但增加了1次内核空间的CPU拷贝，整体上相当于只减少了1次CPU拷贝；
2. 针对大文件比较适合mmap，小文件则会造成较多的内存碎片，得不偿失；
3. 当mmap一个文件时，如果文件被另一个进程截获可能会因为非法访问导致进程被SIGBUS 信号终止；



##### (2)  sendfile

sendfile是在linux2.1引入的，它只需要2次上下文切换和1次内核CPU拷贝、2次DMA拷贝，函数原型

![image-20210601093840397](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20210601093840397.png)

out_fd为文件描述符，in_fd为网络缓冲区描述符，offset偏移量（默认NULL），count文件大小。

它的内部执行流程是这样的

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/XVbUB37bCmAvhcFOPs261qz7h5fWibMVdm2ricdt399xpTNE2N48srAyjcBaB0wia2pFsJ6p4Xl7wgzN96bBLS9hQ/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

1. 用户进程系统调用senfile，由用户态进入内核态，发生第1次上下文切换；
2. CPU通知DMA控制器把文件数据拷贝到内核缓冲区；
3. 内核空间自动调用网络发送功能并拷贝数据到网络缓冲区；
4. CPU通知DMA控制器发送数据；
5. sendfile系统调用结束并返回，进程由内核态进入用户态，发生第2次上下文切换；

**总结**



1. 数据处理完全是由内核操作，减少了2次上下文切换，整个过程2次上下文切换、1次CPU拷贝，2次DMA拷贝；
2. 虽然可以设置偏移量，但不能对数据进行任何的修改；



##### (3) sendfile+DMA gather

sendfile+DMA gather

Linux2.4对sendfile进行了优化，为DMA控制器引入了gather功能，就是在不拷贝数据到网络缓冲区，而是将待发送数据的内存地址和偏移量等描述信息存在网络缓冲区，DMA根据描述信息从内核的读缓冲区截取数据并发送。它的流程是如下

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/XVbUB37bCmAvhcFOPs261qz7h5fWibMVdft8UOVyiaibeMibibnxibLUdb7IPMib6YomUpmY2aq1h3ruapLorvHXfuftw/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

1. 用户进程系统调用senfile，由用户态进入内核态，发生第1次上下文切换；
2. CPU通知DMA控制器把文件数据拷贝到内核缓冲区；
3. 把内核缓冲区地址和sendfile的相关参数作为数据描述信息存在网络缓冲区中；
4. CPU通知DMA控制器，DMA根据网络缓冲区中的数据描述截取数据并发送；
5. sendfile系统调用结束并返回，进程由内核态进入用户态，发生第2次上下文切换；

**总结**

1. 需要硬件支持，如DMA；
2. 整个过程2次上下文切换，0次CPU拷贝，2次DMA拷贝，实现真正意义上的零拷贝；
3. 依然不能修改数据；



总结一下，sendfile系统调用利用DMA引擎将文件内容拷贝到内核缓冲区去，然后将带有文件位置和长度信息的缓冲区描述符添加socket缓冲区去，这一步不会将内核中的数据拷贝到socket缓冲区中，DMA引擎会将内核缓冲区的数据拷贝到协议引擎中去，避免了最后一次拷贝。

![图片](https://mmbiz.qpic.cn/mmbiz_png/1TDxR6xkRSFlHbtX4vP30w4hPqQFF2QTN9MJFZquFpSBNOXusicgjvVsXaq6Y2U0HQbTFDibibJ7e48f38MM3qH8w/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

##### (4) splice 

鉴于Sendfile的缺点，在Linux2.6.17中引入了Splice，它在读缓冲区和网络操作缓冲区之间建立管道避免CPU拷贝：先将文件读入到内核缓冲区，然后再与内核网络缓冲区建立管道。它的函数原型

```
ssize_t splice(int fd_in, loff_t *off_in, int fd_out, loff_t *off_out, size_t len, unsigned int flags);
```

它的执行流程如下

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/XVbUB37bCmAvhcFOPs261qz7h5fWibMVdUjUC7tlrNyltTr3w8WSPpMQ7ruswlicF1CIFr1ia2mAyV85xd5ovm4bQ/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

1. 用户进程系统调用splice，由用户态进入内核态，发生第1次上下文切换；
2. CPU通知DMA控制器把文件数据拷贝到内核缓冲区；
3. 建立内核缓冲区和网络缓冲区的管道；
4. CPU通知DMA控制器，DMA从管道读取数据并发送；
5. splice系统调用结束并返回，进程由内核态进入用户态，发生第2次上下文切换；

**总结**

1. 整个过程2次上下文切换，0次CPU拷贝，2次DMA拷贝，实现真正意义上的零拷贝；
2. 依然不能修改数据；
3. fd_in和fd_out必须有一个是管道；



##### (5) tee

tee与splice类同，但fd_in和fd_out都必须是管道。



写在最后



![图片](https://mmbiz.qpic.cn/mmbiz_jpg/XVbUB37bCmAvhcFOPs261qz7h5fWibMVdYtfmicLanA7F6IpAe68xjDU0ldFXem5XdFmzmbzKgu1zMLExmhWuqIA/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

各种I/O方案总结对比如上。



## 十 Redis