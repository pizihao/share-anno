package com.deep.jsr269;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

/**
 * <h2>类型适配</h2>
 *
 * @author Create by liuwenhao on 2022/8/3 14:58
 */
public interface AttributeAdapt {

    /**
     * 类型
     *
     * @return class
     */
    JCTree clsType(TreeMaker treeMaker, Names names, Symbol.MethodSymbol symbol);

    /**
     * 构建合适的默认值
     *
     * @param treeMaker 语法树工厂
     * @param attribute Attribute
     * @return JCTree
     */
    JCTree.JCExpression buildJCAttribute(TreeMaker treeMaker,
                                         Symbol.MethodSymbol symbol,
                                         Names names,
                                         Attribute attribute);


    default String arrayToString(String s){
        if (!s.contains("[") && !s.contains("]")){
            return s;
        }
        return s.replace("[", "").replace("]", "");
    }
}