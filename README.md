## share-anno

#### 引入依赖

~~~ xml
<dependency>
    <groupId>io.github.pizihao</groupId>
    <artifactId>share-anno</artifactId>
    <version>0.0.1</version>
</dependency>
~~~

> 注意：请确保环境变量配置的jdk路径中存在tools.jar文件，
>
> 同时在 File | Settings | Build, Execution, Deployment | Compiler 中将 Clear output directory on rebuild 打钩，和Shared build process VM options: 配置：-Djps.track.ap.dependencies=false

#### 简单使用

定义一组注解和使用到的类：

1：Blue.class -- 内部含有两个数组类型的方法

~~~java
public @interface Blue {
    Open[] opens();
    TestEnum[] enums();
}
~~~

2：Close.class -- 内部包含多种不同类型的方法

~~~java
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Close {
    int test();
    Class<? extends TestDefult> cls();
    Open open();
    TestEnum enums();
    TestEnum[] arrays();
    Open[] opens();
    Blue[] blues();
}
~~~

3：Open.class -- 仅包含一个String类型的方法

~~~java
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Open {
    String value();
}
~~~

4：Project.class -- 包含一个注解类型的方法

~~~java 
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Project {
    Open name();
}
~~~

5：Model.class -- 无方法，但是使用了其他注解

~~~java
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Project(name = @Open("打开"))
@Close(
    test = 1,
    cls = TestDefult.class,
    open = @Open("注解"),
    enums = TestEnum.DOWN,
    arrays = {TestEnum.DOWN, TestEnum.LEFT, TestEnum.RIGHT, TestEnum.UP},
    opens = {@Open("liu"), @Open("wen"), @Open("hao")},
    blues = {
        @Blue(
            opens = {@Open("liu"), @Open("wen"), @Open("hao")},
            enums = {TestEnum.DOWN, TestEnum.LEFT, TestEnum.RIGHT, TestEnum.UP}
        ),
        @Blue(
            opens = {@Open("xiang"), @Open("chun"), @Open("qu")},
            enums = {TestEnum.DOWN, TestEnum.LEFT, TestEnum.RIGHT}
        )
    }
)
@Open("kaiakaiaia")
public @interface Model {
}

~~~

6：Api.class -- 配置了注解解析器

~~~java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ShareAnnotation(ignore = Project.class, importance = {Close.class, Open.class})
@Model
public @interface Api {
}
~~~

7：TestEnum.class 简单的枚举

~~~java
public enum TestEnum {
    DOWN,
    UP,
    RIGHT,
    LEFT;
}
~~~

8：TestDefult.class -- 简单的类

~~~java
public class TestDefult {

}
~~~



根据以上配置后，执行可以编译整个项目的操作可以得到最终的结果：

Api.class

~~~java
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ShareAnnotation(
    ignore = {Project.class},
    importance = {Close.class, Open.class}
)
@Model
public @interface Api {
    Blue[] blues() default {@Blue(
    opens = {@Open("liu"), @Open("wen"), @Open("hao")},
    enums = {TestEnum.DOWN, TestEnum.LEFT, TestEnum.RIGHT, TestEnum.UP}
), @Blue(
    opens = {@Open("xiang"), @Open("chun"), @Open("qu")},
    enums = {TestEnum.DOWN, TestEnum.LEFT, TestEnum.RIGHT}
)};

    Open open() default @Open("注解");

    String value() default "kaiakaiaia";

    Open[] opens() default {@Open("liu"), @Open("wen"), @Open("hao")};

    TestEnum[] arrays() default {TestEnum.DOWN, TestEnum.LEFT, TestEnum.RIGHT, TestEnum.UP};

    Class<? extends TestDefult> cls() default TestDefult.class;

    int test() default 1;

    TestEnum enums() default TestEnum.DOWN;
}
~~~

该注解内部已经存在了所有的注解，可以通过反射调用

