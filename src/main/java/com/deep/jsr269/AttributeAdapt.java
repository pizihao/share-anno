package com.deep.jsr269;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.Messager;

/**
 * <h2>类型适配</h2>
 *
 * @author Create by liuwenhao on 2022/8/3 14:58
 */
public interface AttributeAdapt {

    /**
     * 类型
     * @return class
     */
    Class<?> clsType();

    /**
     * 构建合适的默认值
     *
     * @param treeMaker 语法树工厂
     * @param attribute Attribute
     * @return JCTree
     */
    JCTree.JCExpression buildJCAttribute(TreeMaker treeMaker, Messager names, Attribute attribute);

}