package org.maximus;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class AnnotationProcessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationProcessor.class);

	public static void main(String[] args) throws IOException {
		FileInputStream in = new FileInputStream("src/resources/Example.java");
		JavaParser javaParser = new JavaParser();
		ParseResult<CompilationUnit> parseResult = javaParser.parse(in);
		if (!parseResult.isSuccessful()) {
			LOGGER.error("Error(s): %s",
					parseResult.getProblems().stream().map(p -> p.getMessage()).collect(Collectors.joining(",")));
			System.exit(1);
		}
		Optional<CompilationUnit> optionalResult = parseResult.getResult();
		if (!optionalResult.isPresent()) {
			LOGGER.error("No result");
			System.exit(1);
		}
		CompilationUnit compilationUnit = optionalResult.get();
		if (compilationUnit.getPackageDeclaration().isPresent()) {
			System.out.println("Package : " + compilationUnit.getPackageDeclaration().get().getNameAsString());
		}
		Optional<ClassOrInterfaceDeclaration> classOrInterface = compilationUnit
				.findAll(ClassOrInterfaceDeclaration.class).stream()
				.filter(classDecl -> classDecl.getParentNode().isPresent()
						&& classDecl.getParentNode().get() instanceof CompilationUnit)
				.findFirst();
		if (classOrInterface.isPresent()) {
			System.out.println("Class : " + classOrInterface.get().getNameAsString());
			compilationUnit.accept(new VoidVisitorAdapter<Object>() {
				@Override
				public void visit(FieldDeclaration fieldDeclaration, final Object argument) {
					for (AnnotationExpr annotation : fieldDeclaration.getAnnotations()) {
						System.out.println("Class annotation: " + annotation.getName());
					}
					Type type = fieldDeclaration.getElementType();
					System.out.println(("  Type: " + type.asString()));
					for (VariableDeclarator variable : fieldDeclaration.getVariables()) {
						System.out.println("Field name: " + variable.getNameAsString());
					}
					super.visit(fieldDeclaration, argument);
				}
			}, null);
		}
	}
}
