package com.deep.jsr269.compound;

import com.deep.jsr269.handler.ShareHandle;
import com.deep.jsr269.model.AnnoMethodDefModel;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.tree.JCTree;

import javax.lang.model.element.Element;
import java.util.Set;

/**
 * 获取注解中的 Compound
 *
 * @author Create by liuwenhao on 2022/8/9 15:56
 */
public interface Compound {

    /**
     * 获取注解方法集合
     *
     * @return AnnoMethodDefModel
     */
    Set<AnnoMethodDefModel> getCompound();


    /**
     * 获取注解类中的方法信息，加入到compoundSet列表中<br>
     * 如：<br>
     * {@code
     *
     * interface A{
     * String B();
     * }
     * <p>
     * }<br>
     * 中的方法 B
     */
    void forJCClassDecl(JCTree.JCClassDecl tree);


    /**
     * 获取在目标类上使用的注解方法信息，加入到compoundSet列表中<br>
     * 如：<br>
     * {@code
     *
     * C(B = "b")
     * interface A{
     * }
     * <p>
     * }<br>
     * 中的方法 B
     */
    void forMirrors(Attribute.Compound mirror);

    /**
     * 向element中加入import语法，从而导入packageModel中的类
     *
     * @param element 元素，一般是类元素，接口元素或注解元素
     */
    void addImportInfo(Element element);

    /**
     * 递归解析节点树，获取其中注解的方法元素
     *
     * @param tree   节点树
     * @param handle 针对注解是否忽略的操作
     * @param trees  解析树
     */
    default void collectCompound(JCTree.JCClassDecl tree,
                                 ShareHandle handle,
                                 JavacTrees trees) {
        forJCClassDecl(tree);

        for (Attribute.Compound mirror : tree.sym.getAnnotationMirrors()) {
            if (handle.isCurrentMirrors(mirror)) {
                forMirrors(mirror);
            }
            if (handle.isNextMirrors(mirror, trees)) {
                JCTree.JCClassDecl mirrorTree = (JCTree.JCClassDecl) trees.getTree(mirror.type.asElement());
                collectCompound(mirrorTree, handle, trees);
            }
        }
    }
}