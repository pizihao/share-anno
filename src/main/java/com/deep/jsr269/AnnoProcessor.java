package com.deep.jsr269;

import com.deep.jsr269.annotation.ShareAnnotation;
import com.deep.jsr269.annotation.SuperadditionAnnotation;
import com.deep.jsr269.compound.Compound;
import com.deep.jsr269.compound.DefaultCompound;
import com.deep.jsr269.model.AnnoMethodDefModel;
import com.google.auto.service.AutoService;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <h2></h2>
 *
 * @author Create by liuwenhao on 2022/8/1 17:26
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes({
        "com.deep.jsr269.annotation.ShareAnnotation",
        "com.deep.jsr269.annotation.SuperadditionAnnotation"
})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SuppressWarnings("all")
public class AnnoProcessor extends AbstractProcessor {
    private Messager messager;
    private JavacTrees trees;
    private TreeMaker treeMaker;
    private Names names;

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
        Set<? extends Element> shareSet = roundEnv.getElementsAnnotatedWith(ShareAnnotation.class);
        Set<? extends Element> superadditionset = roundEnv.getElementsAnnotatedWith(SuperadditionAnnotation.class);
        shareSet.forEach(element -> {
            // 将Element转换为JCTree
            JCTree jcTree = trees.getTree(element);
            jcTree.accept(new TreeTranslator() {
                @Override
                public void visitClassDef(JCTree.JCClassDecl tree) {
                    ShareHandle handle = new ShareHandle(tree);
                    Compound compound = new DefaultCompound(trees, treeMaker, names);

                    compound.collectCompound(tree, handle, trees);
                    ListBuffer<Name> jcMethodDecls = tree.defs.stream()
                            .filter(c -> c.getKind().equals(Tree.Kind.METHOD))
                            .map(c -> ((JCTree.JCMethodDecl) c).getName())
                            .collect(Collectors.toCollection(ListBuffer::new));

                    for (AnnoMethodDefModel defModel : compound.getCompound()) {
                        JCTree.JCMethodDecl modelJcTree = defModel.createJcTree(treeMaker, names);
                        if (!jcMethodDecls.contains(modelJcTree.getName())) {
                            tree.defs = tree.defs.prepend(modelJcTree);
                        }
                    }
                    compound.addImportInfo(element);
                    super.visitClassDef(tree);
                }
            });
        });
        superadditionset.forEach(element -> {
            JCTree jcTree = trees.getTree(element);
            jcTree.accept(new TreeTranslator() {
                @Override
                public void visitClassDef(JCTree.JCClassDecl tree) {
                    // 提取当前注解上存在的注解信息
                    System.out.println("===================");
                    List<Attribute.Compound> annotationMirrors = tree.sym.getAnnotationMirrors();
                    // AnnotationType 代表注解的类型
                    // ElementValues 代表注解中的方法，需要提取Target注解的value方法
                    // 使用以上两个值就可以完成整个注解的追加操作
                    System.out.println("===================");
                    super.visitClassDef(tree);
                }
            });
        });

        return true;
    }
}