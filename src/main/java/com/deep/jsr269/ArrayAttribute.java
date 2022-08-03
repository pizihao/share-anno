package com.deep.jsr269;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.Messager;

/**
 * <h2></h2>
 *
 * @author Create by liuwenhao on 2022/8/3 17:10
 */
public class ArrayAttribute implements AttributeAdapt {
    @Override
    public Class<?> clsType() {
        return Attribute.Array.class;
    }

    @Override
    public JCTree.JCExpression buildJCAttribute(TreeMaker treeMaker, Messager names, Attribute attribute) {
        Attribute.Array value = (Attribute.Array) attribute;
        JCTree.JCExpression type = treeMaker.Type(attribute.type);
        List<Attribute> attributes = value.getValue();
        ListBuffer<JCTree.JCExpression> jcTrees = new ListBuffer<>();
        for (Attribute a : attributes) {
            AttributeAdapt adapt = ProcessorUtil.attributeAdapt(a);
            jcTrees.add(adapt.buildJCAttribute(treeMaker,names, a));
        }
        List<JCTree.JCExpression> sizes = List.of(treeMaker.Literal(jcTrees.size()));
        return treeMaker.NewArray(type, sizes, jcTrees.toList());
    }
}