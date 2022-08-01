package com.deep.jsr269;

import com.google.auto.service.AutoService;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Objects;
import java.util.Set;

/**
 * <h2></h2>
 *
 * @author Create by liuwenhao on 2022/7/28 15:11
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("com.deep.jsr269.TypeName")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class TestProcessor extends AbstractProcessor {


    // 编译时期输入日志的
    private Messager messager;

    // 将Element转换为JCTree的工具,提供了待处理的抽象语法树
    private JavacTrees trees;

    // 封装了创建AST节点的一些方法
    private TreeMaker treeMaker;

    // 提供了创建标识符的方法
    private Names names;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.trees = JavacTrees.instance(processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.treeMaker = TreeMaker.instance(context);
        this.names = Names.instance(context);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 获取被@Type注解标记的所有元素(这个元素可能是类、变量、方法等等)
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(TypeName.class);
        set.forEach(element -> {
            // 将Element转换为JCTree
            JCTree jcTree = trees.getTree(element);
            jcTree.accept(new TreeTranslator() {

                /**
                 * JCTree.Visitor有很多方法，我们可以通过重写对应的方法,(从该方法的形参中)来获取到我们想要的信息:
                 * 如: 重写visitClassDef方法， 获取到类的信息;
                 *     重写visitMethodDef方法， 获取到方法的信息;
                 *     重写visitVarDef方法， 获取到变量的信息;
                 */
                @Override
                public void visitVarDef(JCTree.JCVariableDecl jcVariableDecl) {
                    List<Name> jcMethodDecls = List.nil();
                    // 属性所在的类
                    Symbol owner = jcVariableDecl.sym.owner;
                    JCTree classTree = trees.getTree(owner);
                    if (classTree.getKind().equals(Tree.Kind.CLASS)) {
                        JCTree.JCClassDecl jcClassDecl = (JCTree.JCClassDecl) classTree;
                        Name jcVariableDeclName = jcVariableDecl.getName();
                        TypeName typeName = jcVariableDecl.sym.getAnnotation(TypeName.class);
                        Name newMethodName;
                        if (!Objects.equals(typeName.value(), "")) {
                            newMethodName = names.fromString(typeName.value());
                        } else {
                            newMethodName = getNewMethodName(jcVariableDeclName);
                        }
                        List<JCTree> defs = jcClassDecl.defs;
                        for (JCTree def : defs) {
                            if (def.getKind().equals(Tree.Kind.METHOD)) {
                                JCTree.JCMethodDecl methodDecl = (JCTree.JCMethodDecl) def;
                                jcMethodDecls = jcMethodDecls.append(methodDecl.getName());
                            }
                        }

                        if (!jcMethodDecls.contains(newMethodName)) {
                            // 对于变量进行生成方法的操作
                            messager.printMessage(Diagnostic.Kind.NOTE, "为属性：" + jcVariableDecl.getName() + " 创建类型");
                            treeMaker.pos = jcVariableDecl.pos;
                            jcClassDecl.defs = jcClassDecl.defs.prepend(makeType(jcVariableDecl, newMethodName));
                        }
                    }
                    super.visitVarDef(jcVariableDecl);
                }
            });
        });
        //我们有修改过AST，所以返回true
        return true;
    }

    private JCTree.JCMethodDecl makeType(JCTree.JCVariableDecl jcVariableDecl, Name newMethodName) {
        /*
          JCStatement：声明语法树节点，常见的子类如下
          JCBlock：语句块语法树节点
          JCReturn：return语句语法树节点
          JCClassDecl：类定义语法树节点
          JCVariableDecl：字段/变量定义语法树节点
          JCMethodDecl：方法定义语法树节点
          JCModifiers：访问标志语法树节点
          JCExpression：表达式语法树节点，常见的子类如下
          JCAssign：赋值语句语法树节点
          JCIdent：标识符语法树节点，可以是变量，类型，关键字等等
         */

        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
        statements.append(treeMaker.Return(getType(jcVariableDecl.vartype)));
        JCTree.JCBlock body = treeMaker.Block(0, statements.toList());
        return treeMaker.MethodDef(
            // mods：访问标志
            treeMaker.Modifiers(Flags.PUBLIC),
            // name：方法名
            newMethodName,
            // restype：返回类型
//            treeMaker.Literal("java.lang.reflect.Type"),
            treeMaker.Ident(names.fromString("Type")),
            // typarams：泛型参数列表
            List.nil(),
            // params：参数列表
            List.nil(),
            // thrown：异常声明列表
            List.nil(),
            // 方法体
            body,
            null
        );
    }

    private Name getNewMethodName(Name name) {
        String s = name.toString();
        return names.fromString("_" + s + "Type");
    }

    private JCTree.JCExpression getType(JCTree.JCExpression vartype) {

        Type type = vartype.type;
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
            List<JCTree.JCExpression> of = List.of(treeMaker.Literal(nil.size()));
            // 创建一个class类型的数组
            JCTree.JCNewArray newArray = treeMaker.NewArray(treeMaker.Ident(names.fromString("Class")), of, nil);
            return treeMaker.Apply(
                List.nil(),
                treeMaker.Select(
                    treeMaker.Ident(names.fromString("ParameterizedTypeImpl")),
                    names.fromString("make")
                ),
                List.of(classAccess, newArray)
            );
        } else {
            targetTypeStr = baseTypeStr.substring(baseTypeStr.lastIndexOf(".") + 1);
            return treeMaker.Select(treeMaker.Ident(names.fromString(targetTypeStr)), names._class);
        }
    }
}
