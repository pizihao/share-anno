package com.deep.jsr269.annotation;

import java.lang.annotation.*;

/**
 * 将当前注解所标注的注解追加到指定的类，属性或方法上。其优先级低于{@link ShareAnnotation}.
 * 如果{@link SuperadditionAnnotation}和{@link ShareAnnotation}同时使用那么他们会互相忽略
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SuperadditionAnnotation {

    /**
     * 需要追加注解的目标类，必填
     */
    Class<?> targetClass();

    /**
     * 元素类型，说明这个注解需要加注的那种类型上，如果和这个注解本身的类型不同则忽略，必填
     */
    ElementType type();

}
