package com.deep.jsr269;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractHandler {

    static final String SHARE_ANNOTATION = "com.deep.jsr269.annotation.ShareAnnotation";

    static final String SUPERADDITION_ANNOTATION = "com.deep.jsr269.annotation.SuperadditionAnnotation";

    /**
     * 必须被忽略的注解列表
     */
    protected Set<String> base() {
        Set<String> set = new HashSet<>();
        set.add(SHARE_ANNOTATION);
        set.add(SUPERADDITION_ANNOTATION);
        set.add("java.lang.annotation.Target");
        set.add("java.lang.annotation.Inherited");
        set.add("java.lang.annotation.Retention");
        set.add("java.lang.annotation.Documented");
        set.add("java.lang.annotation.Repeatable");
        return set;
    }
}
