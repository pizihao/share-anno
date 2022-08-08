package com.deep.jsr269.attribute;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

/**
 * <h2>适配{@link Class}类型</h2>
 *
 * @author Create by liuwenhao on 2022/8/3 16:22
 */
public class ClassAttribute implements AttributeAdapt {


    @Override
    public JCTree clsType(TreeMaker treeMaker, Names names, Symbol.MethodSymbol symbol) {
        Type type = symbol.getReturnType();
        String s = type.toString();

        if (s.contains("extends") || s.contains("super")) {
            return treeMaker.Type(type);
        }
        String cls = s.substring(0, s.indexOf("<"));
        return treeMaker.Ident(names.fromString(cls.substring(cls.lastIndexOf(".") + 1)));
    }

    @Override
    public JCTree.JCExpression buildJCAttribute(TreeMaker treeMaker,
                                                Symbol.MethodSymbol symbol,
                                                Names names,
                                                Attribute attribute) {
        Attribute.Class value = (Attribute.Class) attribute;
        Type type = value.getValue();
        List<Type> arguments = type.getTypeArguments();
        JCTree.JCExpression expression = treeMaker.Type(type);
        List<JCTree.JCExpression> jcExpressions = List.nil();
        if (arguments != null && arguments.nonEmpty()) {
            for (Type argument : arguments) {
                String s = argument.toString();
                String cls = s.substring(s.lastIndexOf(".") + 1);
                JCTree.JCExpression ident = treeMaker.Ident(names.fromString(cls));
                jcExpressions = jcExpressions.append(ident);
            }
            return treeMaker.TypeApply(expression, jcExpressions);
        } else {
            String s = type.toString();
            String cls = s.substring(s.lastIndexOf(".") + 1);
            return treeMaker.Select(treeMaker.Ident(names.fromString(cls)), names._class);
        }

    }

}