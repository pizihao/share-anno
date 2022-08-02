package com.deep.jsr269;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Names;

import javax.lang.model.element.Element;
import java.util.ArrayList;
import java.util.Set;

/**
 * @author Create by liuwenhao on 2022/8/2 14:32
 */
public class ProcessorUtil {

    private ProcessorUtil() {

    }

    public static void addImportInfo(Element element,
                                     TreeMaker treeMaker,
                                     JavacTrees trees,
                                     Names names,
                                     Set<PackageModel> packageModel) {
        TreePath treePath = trees.getPath(element);
        Tree leaf = treePath.getLeaf();
        if (treePath.getCompilationUnit() instanceof JCTree.JCCompilationUnit && leaf instanceof JCTree) {
            JCTree.JCCompilationUnit jccu = (JCTree.JCCompilationUnit) treePath.getCompilationUnit();

            for (JCTree.JCImport jcTree : jccu.getImports()) {
                if (jcTree != null && (jcTree.qualid instanceof JCTree.JCFieldAccess)) {
                    JCTree.JCFieldAccess jcFieldAccess = (JCTree.JCFieldAccess) jcTree.qualid;
                    try {
                        PackageModel model = new PackageModel(jcFieldAccess.selected.toString(), jcFieldAccess.name.toString());
                        packageModel.remove(model);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
            java.util.List<JCTree> jcTrees = new ArrayList<>(jccu.defs);
            for (PackageModel model : packageModel) {
                JCTree.JCImport jcImport = treeMaker.Import(
                    treeMaker.Select(
                        treeMaker.Ident(names.fromString(model.packageName)),
                        names.fromString(model.className)),
                    false);
                if (!jcTrees.contains(jcImport)) {
                    jcTrees.add(0, jcImport);
                }
            }
            jccu.defs = List.from(jcTrees);
        }
    }

    public static JCTree getType(Type type,
                                 TreeMaker treeMaker,
                                 Names names) {
        // 主要类型
        Type baseType = type.baseType();
        String baseTypeStr = baseType.toString();

        String targetTypeStr;
        // 存在泛型的情况
        if (baseTypeStr.contains("<")) {
            targetTypeStr = baseTypeStr.substring(0, baseTypeStr.lastIndexOf("<"));
            targetTypeStr = targetTypeStr.substring(targetTypeStr.lastIndexOf(".") + 1);
            JCTree.JCFieldAccess classAccess = treeMaker.Select(treeMaker.Ident(names.fromString(targetTypeStr)), names._class);
            List<Type> typeArguments = type.getTypeArguments();
            List<JCTree.JCExpression> nil = List.nil();
            for (Type typeArgument : typeArguments) {
                String argumentStr = typeArgument.toString();
                String s = argumentStr.substring(argumentStr.lastIndexOf(".") + 1);
                nil = nil.append(treeMaker.Select(treeMaker.Ident(names.fromString(s)), names._class));
            }
            return treeMaker.TypeApply(classAccess, nil);
        } else {
            targetTypeStr = baseTypeStr.substring(baseTypeStr.lastIndexOf(".") + 1);
            return treeMaker.Ident(names.fromString(targetTypeStr));
        }
    }

}