package com.deep.jsr269;

import com.deep.jsr269.model.ImportModel;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

import javax.lang.model.element.Element;
import java.util.ArrayList;
import java.util.Set;

/**
 * 具有强定制化的工具类
 *
 * @author Create by liuwenhao on 2022/8/2 14:32
 */
public class ProcessorUtil {

    private ProcessorUtil() {
    }

    /**
     * 向element中加入import语法，从而导入packageModel中的类
     *
     * @param element      元素，一般是类元素，接口元素或注解元素
     * @param treeMaker    语法树工厂
     * @param trees        语法树操作
     * @param names        names
     * @param packageModel 需要导入的包
     */
    public static void addImportInfo(Element element,
                                     TreeMaker treeMaker,
                                     JavacTrees trees,
                                     Names names,
                                     Set<ImportModel> packageModel) {
        TreePath treePath = trees.getPath(element);
        Tree leaf = treePath.getLeaf();
        if (treePath.getCompilationUnit() instanceof JCTree.JCCompilationUnit && leaf instanceof JCTree) {
            JCTree.JCCompilationUnit jccu = (JCTree.JCCompilationUnit) treePath.getCompilationUnit();

            for (JCTree.JCImport jcTree : jccu.getImports()) {
                if (jcTree != null && (jcTree.qualid instanceof JCTree.JCFieldAccess)) {
                    JCTree.JCFieldAccess jcFieldAccess = (JCTree.JCFieldAccess) jcTree.qualid;
                    ImportModel model = new ImportModel(jcFieldAccess.selected.toString(), jcFieldAccess.name.toString());
                    packageModel.remove(model);
                }
            }
            java.util.List<JCTree> jcTrees = new ArrayList<>(jccu.defs);
            for (ImportModel model : packageModel) {
                JCTree.JCImport jcImport = treeMaker.Import(
                    treeMaker.Select(
                        treeMaker.Ident(names.fromString(model.getPackageName())),
                        names.fromString(model.getClassName())),
                    false);
                if (!jcTrees.contains(jcImport)) {
                    jcTrees.add(0, jcImport);
                }
            }
            jccu.defs = List.from(jcTrees);
        }
    }

}