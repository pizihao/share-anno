package com.deep.jsr269;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TypeName {

    /**
     * 方法名
     */
    String value() default "";

}
