package com.deep.jsr269;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.Messager;

/**
 * <h2></h2>
 *
 * @author Create by liuwenhao on 2022/8/3 17:08
 */
public class CompoundAttribute implements AttributeAdapt {
    @Override
    public Class<?> clsType() {
        return Attribute.Compound.class;
    }

    @Override
    public JCTree.JCExpression buildJCAttribute(TreeMaker treeMaker, Messager names, Attribute attribute) {
        return treeMaker.Annotation(attribute);
    }
}