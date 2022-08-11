package com.deep.jsr269.annotation;

import java.lang.annotation.*;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ShareAnnotation {

    /**
     * 除jdk内置的元注解和本注解外，需要将其字段添加到子注解的注解列表。其优先级低于 {@link ShareAnnotation#ignore()}<br>
     * 如果不设置该值，则默认为添加注解列表上全部的注解类
     *
     * @return Class<?>
     */
    Class<?>[] importance() default {};

    /**
     * 除jdk内置的元注解和本注解外，额外需要忽略的注解列表，其优先级高于 {@link ShareAnnotation#importance()}<br>
     * 如果不设置该值，则默认为空
     *
     * @return Class<?>
     */
    Class<?>[] ignore() default {};


}
