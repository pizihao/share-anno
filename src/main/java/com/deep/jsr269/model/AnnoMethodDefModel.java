package com.deep.jsr269.model;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

import java.util.Objects;

/**
 * 注解方法定义
 *
 * @author Create by liuwenhao on 2022/8/2 14:51
 */
public class AnnoMethodDefModel {

    private String methodName;

    private JCTree returnType;

    private JCTree defaultValue;

    public AnnoMethodDefModel(String methodName, JCTree returnType, JCTree defaultValue) {
        this.methodName = methodName;
        this.returnType = returnType;
        this.defaultValue = defaultValue;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public JCTree getReturnType() {
        return returnType;
    }

    public void setReturnType(JCTree returnType) {
        this.returnType = returnType;
    }

    public JCTree getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(JCTree defaultValue) {
        this.defaultValue = defaultValue;
    }

    public JCTree.JCMethodDecl createJcTree(TreeMaker treeMaker,
                                            Names names) {
        return treeMaker.MethodDef(
            treeMaker.Modifiers(Flags.PUBLIC),
            names.fromString(methodName),
            (JCTree.JCExpression) returnType,
            List.nil(),
            List.nil(),
            List.nil(),
            null,
            (JCTree.JCExpression) defaultValue
        );
    }

    @Override
    public String toString() {
        return "AnnoMethodDefModel{" +
            "methodName='" + methodName + '\'' +
            ", returnType=" + returnType +
            ", defaultValue=" + defaultValue +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnnoMethodDefModel that = (AnnoMethodDefModel) o;
        return Objects.equals(methodName, that.methodName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodName);
    }
}