package com.deep.jsr269;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

/**
 * <h2>适配{@link Class}类型</h2>
 *
 * @author Create by liuwenhao on 2022/8/3 16:22
 */
public class ClassAttribute implements AttributeAdapt {

    @Override
    public Class<?> clsType() {
        return Attribute.Class.class;
    }

    @Override
    public JCTree.JCExpression buildJCAttribute(TreeMaker treeMaker, Messager names, Attribute attribute) {
        Attribute.Class value = (Attribute.Class) attribute;
        Type type = value.getValue();
        List<Type> arguments = type.getTypeArguments();
        JCTree.JCExpression expression = treeMaker.Type(type);
        List<JCTree.JCExpression> jcExpressions;
        if (arguments != null && arguments.nonEmpty()){
            jcExpressions = treeMaker.Types(arguments);
            return treeMaker.TypeApply(expression, jcExpressions);
        }else {
            return expression;
        }

    }

}