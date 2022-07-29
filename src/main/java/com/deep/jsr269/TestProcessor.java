package com.deep.jsr269;

import com.google.auto.service.AutoService;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.*;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

/**
 * <h2></h2>
 *
 * @author Create by liuwenhao on 2022/7/28 15:11
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("com.deep.jsr269.Type")
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
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(Type.class);

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
                        Name newMethodName = getNewMethodName(jcVariableDeclName);
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

        statements.append(treeMaker.Return(treeMaker.Select(treeMaker.Ident(names.fromString("this")), jcVariableDecl.getName())));


        messager.printMessage(Diagnostic.Kind.NOTE, "type：" + jcVariableDecl.getType());

        JCTree.JCExpression jcExpression = getType(jcVariableDecl.vartype);


        JCTree.JCBlock body = treeMaker.Block(0, statements.toList());
        return treeMaker.MethodDef(
            // mods：访问标志
            treeMaker.Modifiers(Flags.PUBLIC),
            // name：方法名
            newMethodName,
            // restype：返回类型
            jcExpression,
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

    private JCTree.JCExpression getType(JCTree.JCExpression vartype) throws ClassNotFoundException {

        JCTree.JCArrayTypeTree jcArrayTypeTree = treeMaker.TypeArray(vartype);
        com.sun.tools.javac.code.Type type = vartype.type;

        JCTree.JCExpression jcBaseExpression = treeMaker.Type(type.baseType());
        List<com.sun.tools.javac.code.Type> typeArguments = type.getTypeArguments();

        List<JCTree.JCExpression> nil = List.nil();
        for (com.sun.tools.javac.code.Type typeArgument : typeArguments) {
            nil = nil.append(treeMaker.Type(typeArgument));
        }

        JCTree.JCTypeApply jcTypeApply = treeMaker.TypeApply(jcBaseExpression, nil);

        JCTree.JCIdent jcIdent = treeMaker.Ident(names.fromString("java.lang.reflect.Type"));

        jcIdent.s


        String extend = "extends";
        String sup = "super";

        com.sun.tools.javac.code.Type baseType = type.baseType();
        String s = baseType.toString();

        // 查询是否存在泛型
        boolean isParameterizedType = s.contains("<") || s.contains(">");
        if (isParameterizedType) {
            // 得到 箭头内部的内容
            String parameter = s.substring(s.indexOf("<") + 1, s.lastIndexOf(">"));
            String base = s.substring(0, s.indexOf("<"));
            Class<?> baseCls = Class.forName(base);
            String[] split = parameter.split(",");
            Class<?>[] arguments = new Class[split.length];
            for (int i = 0; i < split.length; i++) {
                String spl = split[i];
                String practicalType;
                if (spl.contains(extend)) {
                    practicalType = spl.substring(spl.lastIndexOf(extend) + 7).trim();
                } else if (spl.contains(sup)) {
                    practicalType = spl.substring(spl.indexOf(sup) + 5).trim();
                } else {
                    practicalType = spl;
                }
                arguments[i] = Class.forName(practicalType);
            }

            java.lang.reflect.Type make = ParameterizedTypeImpl.make(baseCls, arguments, null);

        }
        return null;
    }
}
