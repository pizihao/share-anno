package com.deep.jsr269;

import java.lang.annotation.*;

/**
 * <h2></h2>
 *
 * @author Create by liuwenhao on 2022/7/28 15:22
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Api {

    String value();

    int order();
}
