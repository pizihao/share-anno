package com.deep.jsr269;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

/**
 * <h2></h2>
 *
 * @author Create by liuwenhao on 2022/8/3 17:08
 */
public class CompoundAttribute implements AttributeAdapt {

    @Override
    public JCTree clsType(TreeMaker treeMaker, Names names, Symbol.MethodSymbol symbol) {
        return treeMaker.Type(symbol.getReturnType());
    }

    @Override
    public JCTree.JCExpression buildJCAttribute(TreeMaker treeMaker,
                                                Symbol.MethodSymbol symbol,
                                                Names names,
                                                Attribute attribute) {
        return treeMaker.Annotation(attribute);
    }
}