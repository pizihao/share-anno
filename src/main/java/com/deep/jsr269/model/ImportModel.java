package com.deep.jsr269.model;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import java.util.Objects;

/**
 * 需要导入的包
 *
 * @author Create by liuwenhao on 2022/8/2 14:17
 */
public class ImportModel {

    String packageName;

    String className;

    public ImportModel(String packageName, String className) {
        this.packageName = packageName;
        this.className = className;
    }

    public ImportModel(JCTree.JCExpression packageName, Name className) {
        this.packageName = packageName.toString();
        this.className = className.toString();
    }

    public JCTree.JCImport jcImport(TreeMaker treeMaker, Names names) {
        return treeMaker.Import(
            treeMaker.Select(
                treeMaker.Ident(names.fromString(getPackageName())),
                names.fromString(getClassName())),
            false);
    }


    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImportModel that = (ImportModel) o;
        return Objects.equals(packageName, that.packageName) && Objects.equals(className, that.className);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageName, className);
    }

    @Override
    public String toString() {
        return "PackageModel{" +
            "packageName='" + packageName + '\'' +
            ", className='" + className + '\'' +
            '}';
    }
}