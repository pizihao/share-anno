package com.deep.jsr269;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import java.lang.reflect.Type;

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
                                                Names names,
                                                Symbol.MethodSymbol symbol,
                                                Attribute attribute,
                                                Messager messager) {
        Attribute.Array value = (Attribute.Array) attribute;
        JCTree.JCExpression type = treeMaker.Type(attribute.type);
        List<Attribute> attributes = value.getValue();
        ListBuffer<JCTree.JCExpression> jcTrees = new ListBuffer<>();
        for (Attribute a : attributes) {
            AttributeAdapt adapt = ProcessorUtil.attributeAdapt(a);
            jcTrees.add(adapt.buildJCAttribute(treeMaker, names, symbol, a, messager));
        }
        List<JCTree.JCExpression> sizes = List.of(treeMaker.Literal(jcTrees.size()));
        JCTree.JCNewArray newArray = treeMaker.NewArray(type, sizes, jcTrees.toList());
        JCTree.JCVariableDecl varDef = treeMaker.VarDef(
            treeMaker.Modifiers(Flags.PUBLIC),
            names.Array,
            treeMaker.TypeArray(type),
            newArray
        );
        messager.printMessage(Diagnostic.Kind.NOTE, "A :" + varDef);
        return ident;

//        JCTree.JCArrayTypeTree arrayTypeTree = treeMaker.TypeArray(type);
//
//        return arrayTypeTree;
    }
}