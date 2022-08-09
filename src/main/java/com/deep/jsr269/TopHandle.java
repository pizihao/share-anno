package com.deep.jsr269;

import com.deep.jsr269.annotation.Top;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 语法树是否可以继续进行的判断条件
 *
 * @author Create by liuwenhao on 2022/8/8 18:46
 */
public class TopHandle {

    static final String TOP_FULL_NAME = "com.deep.jsr269.annotation.Top";
    static final String IGNORE = "ignore";
    static final String IMPORTANCE = "importance";

    /**
     * 忽略列表
     */
    Set<String> noAnno = base();

    /**
     * 关注列表
     */
    Set<String> anceAnno = new HashSet<>();


    public TopHandle(JCTree.JCClassDecl tree) {
        getAnno(tree);
    }

    public Set<String> getNoAnno() {
        return noAnno;
    }

    public Set<String> getAnceAnno() {
        return anceAnno;
    }

    /**
     * 通过忽略列表和关注列表来判断mirror是否在解析目标内<br>
     * 如：A 中存在注解 B。该方法对于B来说是判断是否将B中的方法加入到A中
     *
     * @param mirror 注解语法树信息
     * @return 是否进入下一个注解
     */
    public boolean isCurrentMirrors(Attribute.Compound mirror) {
        String annoName = mirror.getAnnotationType().toString();
        return !noAnno.contains(annoName) && (anceAnno.isEmpty() || anceAnno.contains(annoName));
    }

    /**
     * 通过忽略列表和关注列表来判断mirror是否在解析目标内<br>
     * 如：A 中存在注解 B。B存在注解C，D。该方法对于B来说是判断是否将B作为一个类来解析其类注解，并将C，D中的方法加入到A中
     *
     * @param mirror 注解语法树信息
     * @return 是否进入下一个注解
     */
    public boolean isNextMirrors(Attribute.Compound mirror, JavacTrees trees) {
        String annoName = mirror.getAnnotationType().toString();
        return !noAnno.contains(annoName)
            && trees.getTree(mirror.type.asElement()).getKind().equals(Tree.Kind.ANNOTATION_TYPE);
    }

    /**
     * 将jdk内置的元注解和jsr启动的注解添加到忽略列表中
     *
     * @param decl JCClassDecl类的语法数解析对象
     */
    private void getAnno(JCTree.JCClassDecl decl) {
        if (decl == null || decl.sym == null) {
            return;
        }
        List<Attribute.Compound> mirrors = decl.sym.getAnnotationMirrors();
        for (Attribute.Compound mirror : mirrors) {
            String s = mirror.getAnnotationType().toString();
            if (s != null && s.equals(TOP_FULL_NAME)) {
                noAnno.addAll(ignoreAnno(mirror));
                anceAnno.addAll(importanceAnno(mirror));
            }
        }
    }

    /**
     * 提取{@link Top#ignore()}，解析时忽略的注解
     *
     * @param mirror 适用于注解的Attribute
     * @return Set 注解类型类形式的字符串
     */
    private Set<String> ignoreAnno(Attribute.Compound mirror) {
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
    private Set<String> importanceAnno(Attribute.Compound mirror) {
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

    /**
     * 必须被忽略的注解列表
     */
    private Set<String> base() {
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