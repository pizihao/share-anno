package com.deep.jsr269;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * <h2></h2>
 *
 * @author Create by liuwenhao on 2022/8/8 18:46
 */
public class TopHandle {

    /**
     * 提取全部的注解信息
     *
     * @param mirrors 注解列表
     * @return ListBuffer
     */
    public static ListBuffer<String> allAnno(List<Attribute.Compound> mirrors) {
        if (mirrors == null) {
            return new ListBuffer<>();
        }
        return mirrors.stream().map(c -> c != null ? cutting(c.toString()) : null)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toCollection(ListBuffer::new));
    }

    static String cutting(String str) {
        if (str.contains("(")) {
            return str.substring(str.indexOf("@"), str.indexOf("("));
        }
        return str.substring(str.indexOf("@"));
    }

    public static List<String> base(){
        return List.of(
            "java.lang.annotation.Target",
            "java.lang.annotation.Retention",
            "java.lang.annotation.Documented",
            "java.lang.annotation.Inherited",
            "java.lang.annotation.Repeatable",
            "com.deep.jsr269.Top"
        );
    }

}