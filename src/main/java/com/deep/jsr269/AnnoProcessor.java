package com.deep.jsr269;

import com.deep.jsr269.annotation.Top;
import com.deep.jsr269.attribute.AttributeAdapt;
import com.deep.jsr269.model.AnnoMethodDefModel;
import com.deep.jsr269.model.PackageModel;
import com.google.auto.service.AutoService;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <h2></h2>
 *
 * @author Create by liuwenhao on 2022/8/1 17:26
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("com.deep.jsr269.annotation.Top")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AnnoProcessor extends AbstractProcessor {
    @SuppressWarnings("all")
    private Messager messager;
    private JavacTrees trees;
    private TreeMaker treeMaker;
    private Names names;

    private final Set<PackageModel> packageModels = new HashSet<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        if (!(processingEnv instanceof JavacProcessingEnvironment)) {
            throw new IllegalArgumentException();
        }
        super.init(processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.treeMaker = TreeMaker.instance(context);
        this.names = Names.instance(context);
        this.messager = processingEnv.getMessager();
        this.trees = JavacTrees.instance(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 全部都是注解元素
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(Top.class);
        set.forEach(element -> {
            // 将Element转换为JCTree
            JCTree jcTree = trees.getTree(element);
            jcTree.accept(new TreeTranslator() {
                @Override
                public void visitClassDef(JCTree.JCClassDecl tree) {
                    Set<String> noAnno = TopHandle.base();
                    Set<String> anceAnno = new HashSet<>();
                    Set<AnnoMethodDefModel> compoundSet = new HashSet<>();
                    getAnno(tree, noAnno, anceAnno);
                    getCompound(tree, noAnno, anceAnno, compoundSet);
                    List<Name> jcMethodDecls = List.nil();
                    List<JCTree> defs = tree.defs;
                    for (JCTree def : defs) {
                        if (def.getKind().equals(Tree.Kind.METHOD)) {
                            JCTree.JCMethodDecl methodDecl = (JCTree.JCMethodDecl) def;
                            jcMethodDecls = jcMethodDecls.append(methodDecl.getName());
                        }
                    }
                    for (AnnoMethodDefModel defModel : compoundSet) {
                        JCTree.JCMethodDecl modelJcTree = defModel.createJcTree(treeMaker, names);
                        if (!jcMethodDecls.contains(modelJcTree.getName())) {
                            tree.defs = tree.defs.prepend(modelJcTree);
                        }
                    }
                    ProcessorUtil.addImportInfo(element, treeMaker, trees, names, packageModels);
                    super.visitClassDef(tree);
                }
            });
        });
        return true;
    }

    private void getCompound(JCTree.JCClassDecl tree,
                             Set<String> noAnno,
                             Set<String> anceAnno,
                             Set<AnnoMethodDefModel> compoundSet) {
        forJCClassDecl(tree, compoundSet);
        List<Attribute.Compound> mirrors = tree.sym.getAnnotationMirrors();
        for (Attribute.Compound mirror : mirrors) {
            /*
            messager.printMessage(Diagnostic.Kind.NOTE, "内容 ：" + mirror);
             */
            String declaredType = mirror.getAnnotationType().toString();
            if (!noAnno.contains(declaredType)) {
                if (anceAnno.isEmpty() || anceAnno.contains(declaredType)){
                    forMirrors(mirror, compoundSet);
                }
                JCTree mirrorTree = trees.getTree(mirror.type.asElement());
                if (mirrorTree.getKind().equals(Tree.Kind.ANNOTATION_TYPE)) {
                    getCompound((JCTree.JCClassDecl) mirrorTree, noAnno, anceAnno, compoundSet);
                }
            }
        }
    }

    private void forMirrors(Attribute.Compound mirror, Set<AnnoMethodDefModel> compoundSet) {
        // 获取注解类上已有的声明，放入 compoundSet 中
        Map<Symbol.MethodSymbol, Attribute> elementValues = mirror.getElementValues();
        elementValues.forEach((m, a) -> {
            AttributeAdapt adapt = ProcessorUtil.attributeAdapt(a);
            JCTree returnType = adapt.clsType(treeMaker, names, m);
            JCTree.JCExpression defaultValue = adapt.buildJCAttribute(treeMaker, m, names, a);
            if (returnType != null && defaultValue != null) {
                String methodName = m.getSimpleName().toString();
                compoundSet.add(new AnnoMethodDefModel(methodName, returnType, defaultValue));
            }
        });
    }

    /**
     * 将当前注解类中可以添加的方法追加到集合中
     *
     * @param tree        当前类的语法树
     * @param compoundSet 方法集合
     */
    private void forJCClassDecl(JCTree.JCClassDecl tree, Set<AnnoMethodDefModel> compoundSet) {
        setPackageModels(tree);
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
                    compoundSet.add(new AnnoMethodDefModel(methodName, returnType, defaultValue));
                }
            }
        }
    }

    private void setPackageModels(JCTree.JCClassDecl tree) {
        // 包
        TreePath path = trees.getPath(tree.sym);
        Tree leaf = path.getLeaf();
        if (path.getCompilationUnit() instanceof JCTree.JCCompilationUnit && leaf instanceof JCTree) {
            JCTree.JCCompilationUnit compilationUnit = (JCTree.JCCompilationUnit) path.getCompilationUnit();
            for (JCTree.JCImport jcImport : compilationUnit.getImports()) {
                if (jcImport != null && (jcImport.qualid instanceof JCTree.JCFieldAccess)) {
                    JCTree.JCFieldAccess jcFieldAccess = (JCTree.JCFieldAccess) jcImport.qualid;

                    String selected = jcFieldAccess.selected.toString();
                    String name = jcFieldAccess.name.toString();
                    packageModels.add(new PackageModel(selected, name));
                }
            }
        }
    }

    /**
     * 将jdk内置的元注解和jsr启动的注解添加到忽略列表中
     *
     * @param decl     JCClassDecl类的语法数解析对象
     * @param anceAnno 添加的注解列表
     * @param noAnno   忽略的注解列表
     */
    private void getAnno(JCTree.JCClassDecl decl, Set<String> noAnno, Set<String> anceAnno) {
        if (decl == null || decl.sym == null) {
            return;
        }
        List<Attribute.Compound> mirrors = decl.sym.getAnnotationMirrors();
        for (Attribute.Compound mirror : mirrors) {
            String s = mirror.getAnnotationType().toString();
            if (s != null && s.equals(TopHandle.TOP_FULL_NAME)) {
                noAnno.addAll(TopHandle.ignoreAnno(mirror));
                anceAnno.addAll(TopHandle.importanceAnno(mirror));
            }
        }
    }
}