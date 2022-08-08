package com.deep.jsr269;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.Messager;

/**
 * <h2></h2>
 *
 * @author Create by liuwenhao on 2022/8/3 17:04
 */
public class EnumAttribute implements AttributeAdapt {
    @Override
    public JCTree clsType(TreeMaker treeMaker, Names names, Symbol.MethodSymbol symbol) {
        return treeMaker.Type(symbol.getReturnType());
    }

    @Override
    public JCTree.JCExpression buildJCAttribute(TreeMaker treeMaker,
                                                Names names,
                                                Symbol.MethodSymbol symbol,
                                                Attribute attribute,
                                                Messager messager) {
        String s = arrayToString(symbol.getReturnType().toString());
        String cls = s.substring(s.lastIndexOf(".") + 1);
        return treeMaker.Select(
            treeMaker.Ident(names.fromString(cls)),
            names.fromString(attribute.getValue().toString())
        );
    }
}