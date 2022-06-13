package dev.vankka.dynamicproxy.processor;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.printer.Printer;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.sun.source.util.Trees;
import dev.vankka.dynamicproxy.DynamicProxy;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.*;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("dev.vankka.dynamicproxy.processor.Proxy")
public class DynamicProxyProcessor extends AbstractProcessor {

    private static final String DYNAMIC_PROXY_CLASS_NAME = DynamicProxy.class.getName();

    @Override
    public SourceVersion getSupportedSourceVersion() {
        SourceVersion version = SourceVersion.latestSupported();
        SourceVersion minimumVersion = SourceVersion.RELEASE_8;
        SourceVersion maximumVersion;
        try {
            // Based on the maximum version completely supported by the underlying parsing/generation library
            maximumVersion = SourceVersion.valueOf("RELEASE_13");
        } catch (IllegalArgumentException ignored) {
            maximumVersion = SourceVersion.latestSupported();
        }

        if (version.ordinal() < minimumVersion.ordinal()) {
            return minimumVersion;
        } else if (version.ordinal() > maximumVersion.ordinal()) {
            // Better warning
            this.processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.NOTE,
                    "Supported source version '" + maximumVersion
                            + "' from annotation processor '" + getClass().getName()
                            + "' less than " + version);
            return version; // ignores the JVM error which usually just shows Gradle's TimeTrackingProcessor anyways
        } else {
            // Within supported range
            return version;
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Trees trees = Trees.instance(jbUnwrap(ProcessingEnvironment.class, processingEnv));

        for (Element element : roundEnvironment.getElementsAnnotatedWith(Proxy.class)) {
            processElement(element, trees);
        }
        return false;
    }

    private void processElement(Element element, Trees trees) {
        Proxy proxy = element.getAnnotation(Proxy.class);

        TypeMirror typeMirror;
        try {
            typeMirror = processingEnv.getElementUtils().getTypeElement(proxy.value().getName()).asType();
        } catch (MirroredTypeException e) {
            typeMirror = e.getTypeMirror();
        }

        if (!(typeMirror instanceof DeclaredType)
                || ((DeclaredType) typeMirror).asElement().getKind() != ElementKind.INTERFACE) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "The value of @Proxy needs to be a interface", element);
            return;
        }

        JavaFileObject object = trees.getPath(element)
                .getCompilationUnit()
                .getSourceFile();

        JavaParser javaParser = new JavaParser(new ParserConfiguration().setSymbolResolver(new JavaSymbolSolver(new ReflectionTypeSolver(false))));
        CompilationUnit compilationUnit;
        try (InputStream inputStream = object.openInputStream()) {
            ParseResult<CompilationUnit> parseResult = javaParser.parse(inputStream);
            compilationUnit = parseResult.getResult().orElse(null);
            if (compilationUnit == null) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Java parse failure: " + parseResult.getProblem(0).getMessage());
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        String originalField = null;
        if (element instanceof TypeElement) {
            for (Element enclosedElement : element.getEnclosedElements()) {
                if (enclosedElement.getKind() != ElementKind.FIELD) {
                    continue;
                }
                if (enclosedElement.getAnnotation(Original.class) == null) {
                    continue;
                }
                if (!processingEnv.getTypeUtils().isAssignable(typeMirror, enclosedElement.asType())) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@Original needs to be assignable to " + typeMirror, enclosedElement);
                    return;
                }
                if (originalField != null) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Duplicate @Original field", enclosedElement);
                    return;
                }

                originalField = enclosedElement.getSimpleName().toString();
            }
        }
        String originalFieldName = originalField != null ? "this." + originalField : "original";

        String packageName = processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
        String originalName = element.getSimpleName().toString();
        String proxyName = proxy.className().isEmpty() ? originalName + proxy.suffix() : proxy.className();
        String proxyTypeName = ((TypeElement) (((DeclaredType) typeMirror).asElement())).getQualifiedName().toString();

        String finalOriginalField = originalField;
        System.out.println("pre: " + compilationUnit);

        compilationUnit.accept(new ModifierVisitor<Void>() {

            @Override
            public Visitable visit(PackageDeclaration n, Void arg) {
                n.setComment(new JavadocComment("Class generated by DynamicProxy"));
                return super.visit(n, arg);
            }

            @Override
            public Visitable visit(final ClassOrInterfaceDeclaration n, final Void arg) {
//                if (!n.getNameAsString().equals(originalName)) {
//                    n.remove();
//                    return super.visit(n, arg);
//                } if (!n.getParentNode().filter(no -> no instanceof CompilationUnit).isPresent()) {
//                    n.removeModifier(Modifier.Keyword.STATIC);
//                    n.setParentNode(compilationUnit);
//                }

                NodeList<AnnotationExpr> annotations = n.getAnnotations();
                annotations.removeIf(annotation -> annotation.resolve().getQualifiedName().equals(Proxy.class.getName()));
                n.setAnnotations(annotations);
                n.setName(proxyName);
                n.removeModifier(Modifier.Keyword.ABSTRACT);
                n.setImplementedTypes(new NodeList<>());

                n.addFieldWithInitializer(
                        DYNAMIC_PROXY_CLASS_NAME,
                        "$DYNAMICPROXY",
                        new MethodCallExpr(
                                "new " + DYNAMIC_PROXY_CLASS_NAME,
                                new ClassExpr(javaParser.parseClassOrInterfaceType(proxyName).getResult().orElseThrow(IllegalStateException::new))
                        ),
                        Modifier.Keyword.PRIVATE,
                        Modifier.Keyword.STATIC,
                        Modifier.Keyword.FINAL
                );

                n.addFieldWithInitializer(
                        proxyTypeName,
                        "$PROXY",
                        new NullLiteralExpr(),
                        Modifier.Keyword.PRIVATE
                );

                MethodDeclaration method = n.addMethod("getProxy", Modifier.Keyword.PUBLIC, Modifier.Keyword.FINAL);
                method.setType(proxyTypeName);

                ClassOrInterfaceType proxyType = javaParser.parseClassOrInterfaceType(proxyTypeName).getResult().orElseThrow(IllegalStateException::new);

                if (finalOriginalField == null) {
                    NodeList<Parameter> nodeList = new NodeList<>();
                    nodeList.add(new Parameter(proxyType, "original"));
                    method.setParameters(nodeList);
                }
                BlockStmt body = new BlockStmt();
                if (finalOriginalField == null) {
                    body.addStatement(
                            new IfStmt(
                                    new BinaryExpr(
                                            new NameExpr("original"),
                                            new NullLiteralExpr(),
                                            BinaryExpr.Operator.EQUALS
                                    ),
                                    new ThrowStmt(
                                            new MethodCallExpr(
                                                    "new java.lang.NullPointerException",
                                                    new StringLiteralExpr("original")
                                            )
                                    ),
                                    null
                            )
                    );
                }
                body.addStatement(
                        new ReturnStmt(
                                new ConditionalExpr(
                                        new BinaryExpr(
                                                new NameExpr("$PROXY"),
                                                new NullLiteralExpr(),
                                                BinaryExpr.Operator.NOT_EQUALS
                                        ),
                                        new NameExpr("$PROXY"),
                                        new EnclosedExpr(
                                                new AssignExpr(
                                                        new NameExpr("$PROXY"),
                                                        new CastExpr(
                                                                proxyType,
                                                                new MethodCallExpr(
                                                                        "$DYNAMICPROXY.make",
                                                                        new NameExpr(originalFieldName),
                                                                        new ThisExpr()
                                                                )
                                                        ),
                                                        AssignExpr.Operator.ASSIGN
                                                )
                                        )
                                )
                        )
                );
                method.setBody(body);

                return super.visit(n, arg);
            }

            @Override
            public Visitable visit(final ConstructorDeclaration n, final Void arg) {
                n.setName(proxyName);
                n.getBody().accept(new ModifierVisitor<Void>() {

                    @Override
                    public Visitable visit(final ExplicitConstructorInvocationStmt n, final Void arg) {
                        n.remove();
                        return super.visit(n, arg);
                    }

                }, null);
                return super.visit(n, arg);
            }

            @Override
            public Visitable visit(MethodDeclaration n, Void arg) {
                NodeList<AnnotationExpr> annotations = n.getAnnotations();
                annotations.removeIf(annotation -> annotation.resolve().getQualifiedName().equals(Override.class.getName()));
                n.setAnnotations(annotations);
                return super.visit(n, arg);
            }
        }, null);

        System.out.println("end: " + compilationUnit + " (" + compilationUnit.getChildNodes().stream().map(no -> no.getClass().getName()).collect(
                Collectors.joining(", ")) + ")");

        String code;
        try {
            Class<CompilationUnit> cuClass = CompilationUnit.class;
            Method getPrinter = cuClass.getDeclaredMethod("getPrinter");
            getPrinter.setAccessible(true);
            Printer printer = (Printer) getPrinter.invoke(compilationUnit);
            code = printer.print(compilationUnit);
        } catch (ReflectiveOperationException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to generate: " + e);
            return;
        }

        try {
            JavaFileObject javaFileObject = processingEnv.getFiler()
                    .createSourceFile(packageName + "." + proxyName);
            try (PrintWriter printWriter = new PrintWriter(javaFileObject.openWriter())) {
                printWriter.write(code);
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to save output: " + e);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static <T> T jbUnwrap(Class<? extends T> iface, T wrapper) {
        T unwrapped = null;
        try {
            final Class<?> apiWrappers = wrapper.getClass().getClassLoader().loadClass("org.jetbrains.jps.javac.APIWrappers");
            final Method unwrapMethod = apiWrappers.getDeclaredMethod("unwrap", Class.class, Object.class);
            unwrapped = iface.cast(unwrapMethod.invoke(null, iface, wrapper));
        } catch (Throwable ignored) {}
        return unwrapped != null? unwrapped : wrapper;
    }
}
