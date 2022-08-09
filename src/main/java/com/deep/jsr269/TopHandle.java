package com.deep.jsr269;

import com.deep.jsr269.annotation.Top;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.util.List;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * <h2></h2>
 *
 * @author Create by liuwenhao on 2022/8/8 18:46
 */
public class TopHandle {

    public static final String TOP_FULL_NAME = "com.deep.jsr269.annotation.Top";
    private static final String IGNORE = "ignore";
    private static final String IMPORTANCE = "importance";

    private TopHandle() {
    }

    /**
     * 提取全部的注解信息
     *
     * @param mirrors 注解列表
     * @return ListBuffer
     */
    public static Set<String> allAnno(List<Attribute.Compound> mirrors) {
        if (mirrors == null) {
            return new HashSet<>();
        }
        return mirrors.stream()
            .map(c -> c != null ? c.getAnnotationType().toString() : null)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    /**
     * 提取{@link Top#ignore()}，解析时忽略的注解
     *
     * @param mirror 适用于注解的Attribute
     * @return Set 注解类型类形式的字符串
     */
    public static Set<String> ignoreAnno(Attribute.Compound mirror) {
        String s = mirror.getAnnotationType().toString();
        if (!s.equals(TOP_FULL_NAME)) {
            return new HashSet<>();
        }
        return mirror.values.stream()
            .filter(m -> IGNORE.equals(m.fst.getSimpleName().toString()))
            .map(m -> ((Attribute.Array) m.snd).getValue()
                .stream()
                .map(c -> c.getValue().toString())
                .collect(Collectors.toSet()))
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());
    }

    /**
     * 提取{@link Top#importance()} ，解析时关注的注解
     *
     * @param mirror 适用于注解的Attribute
     * @return Set 注解类型类形式的字符串
     */
    public static Set<String> importanceAnno(Attribute.Compound mirror) {
        String s = mirror.getAnnotationType().toString();
        if (!s.equals(TOP_FULL_NAME)) {
            return new HashSet<>();
        }
        return mirror.values.stream()
            .filter(m -> IMPORTANCE.equals(m.fst.getSimpleName().toString()))
            .map(m -> ((Attribute.Array) m.snd).getValue()
                .stream()
                .map(c -> c.getValue().toString())
                .collect(Collectors.toSet()))
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());
    }

    public static Set<String> base() {
        Set<String> set = new HashSet<>();
        set.add(TOP_FULL_NAME);
        set.add("java.lang.annotation.Target");
        set.add("java.lang.annotation.Inherited");
        set.add("java.lang.annotation.Retention");
        set.add("java.lang.annotation.Documented");
        set.add("java.lang.annotation.Repeatable");
        return set;
    }

}