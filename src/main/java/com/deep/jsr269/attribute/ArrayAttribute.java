package com.deep.jsr269.attribute;

import com.deep.jsr269.ProcessorUtil;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Names;

/**
 * <h2></h2>
 *
 * @author Create by liuwenhao on 2022/8/3 17:10
 */
public class ArrayAttribute implements AttributeAdapt {

    @Override
    public JCTree clsType(TreeMaker treeMaker, Names names, Symbol.MethodSymbol symbol) {
        return treeMaker.Type(symbol.getReturnType());
    }

    @Override
    public JCTree.JCExpression buildJCAttribute(TreeMaker treeMaker,
                                                Symbol.MethodSymbol symbol,
                                                Names names,
                                                Attribute attribute) {
        Attribute.Array value = (Attribute.Array) attribute;
        List<Attribute> attributes = value.getValue();
        ListBuffer<JCTree.JCExpression> jcTrees = new ListBuffer<>();
        for (Attribute a : attributes) {
            AttributeAdapt adapt = ProcessorUtil.attributeAdapt(a);
            jcTrees.add(adapt.buildJCAttribute(treeMaker, symbol, names, a));
        }
        List<JCTree.JCExpression> sizes = List.of(treeMaker.Literal(jcTrees.size()));
        return treeMaker.NewArray(null, sizes, jcTrees.toList());

    }
}