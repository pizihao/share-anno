package com.deep.jsr269.compound;

import com.deep.jsr269.attribute.AttributeAdapt;
import com.deep.jsr269.attribute.AttributeHelper;
import com.deep.jsr269.model.AnnoMethodDefModel;
import com.deep.jsr269.model.ImportModel;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

import javax.lang.model.element.Element;
import java.util.*;

/**
 * <h2></h2>
 *
 * @author Create by liuwenhao on 2022/8/9 17:00
 */
public class DefaultCompound implements Compound {

    JavacTrees trees;
    TreeMaker treeMaker;
    Names names;
    Set<ImportModel> importModels = new HashSet<>();

    Set<AnnoMethodDefModel> methodDefModels = new HashSet<>();

    public DefaultCompound(JavacTrees trees, TreeMaker treeMaker, Names names) {
        this.trees = trees;
        this.treeMaker = treeMaker;
        this.names = names;
    }


    @Override
    public Set<AnnoMethodDefModel> getCompound() {
        return methodDefModels;
    }

    @Override
    public void forJCClassDecl(JCTree.JCClassDecl tree) {
        collectImport(tree);
        // 获取所有的内部方法，并放到 compoundSet 中
        for (JCTree def : tree.defs) {
            if (def.getKind().equals(Tree.Kind.METHOD)) {
                JCTree.JCMethodDecl methodDecl = (JCTree.JCMethodDecl) def;
                // 如果返回值为null，则为必填的方法，这样的方法是在上一层中进行处理的
                JCTree returnType = methodDecl.getReturnType();
                JCTree defaultValue = methodDecl.getDefaultValue();
                if (returnType != null && defaultValue != null) {
                    String methodName = methodDecl.getName().toString();
                    // 如果已经存在则不覆盖，距离子注解较进的一定要比距离更远的优先级高
                    methodDefModels.add(new AnnoMethodDefModel(methodName, returnType, defaultValue));
                }
            }
        }
    }

    @Override
    public void forMirrors(Attribute.Compound mirror) {
        // 获取注解类上已有的声明，放入 compoundSet 中
        Map<Symbol.MethodSymbol, Attribute> elementValues = mirror.getElementValues();
        elementValues.forEach((m, a) -> {
            AttributeAdapt adapt = AttributeHelper.attributeAdapt(a);
            JCTree returnType = adapt.clsType(treeMaker, names, m);
            JCTree.JCExpression defaultValue = adapt.buildJCAttribute(treeMaker, m, names, a);
            if (returnType != null && defaultValue != null) {
                String methodName = m.getSimpleName().toString();
                methodDefModels.add(new AnnoMethodDefModel(methodName, returnType, defaultValue));
            }
        });
    }

    public void collectImport(JCTree.JCClassDecl tree) {
        // 包
        TreePath path = trees.getPath(tree.sym);
        JCTree.JCCompilationUnit compilationUnit = (JCTree.JCCompilationUnit) path.getCompilationUnit();

        compilationUnit.getImports()
            .stream().filter(Objects::nonNull)
            .filter(c -> c.qualid instanceof JCTree.JCFieldAccess)
            .forEach(c -> {
                JCTree.JCFieldAccess access = (JCTree.JCFieldAccess) c.qualid;
                importModels.add(new ImportModel(access.selected, access.name));
            });
    }


    /**
     * 向element中加入import语法，从而导入packageModel中的类
     *
     * @param element 元素，一般是类元素，接口元素或注解元素
     */
    public void addImportInfo(Element element) {
        TreePath treePath = trees.getPath(element);
        JCTree.JCCompilationUnit compilationUnit = (JCTree.JCCompilationUnit) treePath.getCompilationUnit();

        compilationUnit.getImports().stream()
            .filter(Objects::nonNull)
            .filter(c -> c.getQualifiedIdentifier() instanceof JCTree.JCFieldAccess)
            .map(c -> (JCTree.JCFieldAccess) c.getQualifiedIdentifier())
            .forEach(j -> importModels.remove(new ImportModel(j.selected, j.name)));

        List<JCTree> jcTrees = new ArrayList<>(compilationUnit.defs);

        for (ImportModel model : importModels) {
            JCTree.JCImport jcImport = model.jcImport(treeMaker,names);
            if (!jcTrees.contains(jcImport)) {
                jcTrees.add(0, jcImport);
            }
            compilationUnit.defs = com.sun.tools.javac.util.List.from(jcTrees);
        }
    }
}