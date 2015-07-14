package com.coverity.security.sql;

import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryGenerator {

    private static Type classToType(AST ast, Class<?> clazz) {
        if (clazz.isPrimitive()) {
            if (clazz == boolean.class) {
                return ast.newPrimitiveType(PrimitiveType.BOOLEAN);
            } else if (clazz == byte.class) {
                return ast.newPrimitiveType(PrimitiveType.BYTE);
            } else if (clazz == char.class) {
                return ast.newPrimitiveType(PrimitiveType.CHAR);
            } else if (clazz == double.class) {
                return ast.newPrimitiveType(PrimitiveType.DOUBLE);
            } else if (clazz == float.class) {
                return ast.newPrimitiveType(PrimitiveType.FLOAT);
            } else if (clazz == int.class) {
                return ast.newPrimitiveType(PrimitiveType.INT);
            } else if (clazz == long.class) {
                return ast.newPrimitiveType(PrimitiveType.LONG);
            } else if (clazz == short.class) {
                return ast.newPrimitiveType(PrimitiveType.SHORT);
            } else {
                throw new IllegalStateException();
            }
        } else if (clazz.isArray()) {
            return ast.newArrayType(classToType(ast, clazz.getComponentType()));
        } else {
            return ast.newSimpleType(ast.newName(clazz.getName()));
        }
    }

    private static Type classToBoxType(AST ast, Class<?> clazz) {
        if (clazz.isPrimitive()) {
            if (clazz == boolean.class) {
                return ast.newSimpleType(ast.newName(Boolean.class.getName()));
            } else if (clazz == byte.class) {
                return ast.newSimpleType(ast.newName(Byte.class.getName()));
            } else if (clazz == char.class) {
                return ast.newSimpleType(ast.newName(Character.class.getName()));
            } else if (clazz == double.class) {
                return ast.newSimpleType(ast.newName(Double.class.getName()));
            } else if (clazz == float.class) {
                return ast.newSimpleType(ast.newName(Float.class.getName()));
            } else if (clazz == int.class) {
                return ast.newSimpleType(ast.newName(Integer.class.getName()));
            } else if (clazz == long.class) {
                return ast.newSimpleType(ast.newName(Long.class.getName()));
            } else if (clazz == short.class) {
                return ast.newSimpleType(ast.newName(Short.class.getName()));
            } else {
                throw new IllegalStateException();
            }
        } else if (clazz.isArray()) {
            // Use primitive type here, not boxed type.
            return ast.newArrayType(classToType(ast, clazz.getComponentType()));
        } else {
            return ast.newSimpleType(ast.newName(clazz.getName()));
        }
    }

    public static void main(String[] args) {

        if (args.length != 1) {
            System.err.println("Expects exactly one argument (the output directory)");
            System.exit(1);
        }
        final File outputDir = new File(args[0]);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        AST ast = AST.newAST(AST.JLS8);
        CompilationUnit unit = ast.newCompilationUnit();
        PackageDeclaration packageDeclaration = ast.newPackageDeclaration();
        packageDeclaration.setName(ast.newName("com.coverity.security.sql"));
        unit.setPackage(packageDeclaration);

        ImportDeclaration importDeclaration = ast.newImportDeclaration();
        QualifiedName name =ast.newQualifiedName(ast.newSimpleName("java"), ast.newSimpleName("sql"));
        importDeclaration.setName(name);
        importDeclaration.setOnDemand(true);
        unit.imports().add(importDeclaration);

        /* Type decleration and annotation */
        TypeDeclaration type = ast.newTypeDeclaration();
        type.setInterface(false);
        type.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
        type.setName(ast.newSimpleName("MemoryPreparedStatement"));
        NormalAnnotation generatedAnnotation = ast.newNormalAnnotation();
        generatedAnnotation.setTypeName(ast.newName("javax.annotation.Generated"));
        MemberValuePair value = ast.newMemberValuePair();
        StringLiteral strLit = ast.newStringLiteral();
        strLit.setLiteralValue(MemoryGenerator.class.getName());
        value.setName(ast.newSimpleName("value"));
        value.setValue(strLit);
        generatedAnnotation.values().add(value);
        value = ast.newMemberValuePair();
        value.setName(ast.newSimpleName("date"));
        strLit = ast.newStringLiteral();
        strLit.setLiteralValue(new java.util.Date().toString());
        value.setValue(strLit);
        generatedAnnotation.values().add(value);
        type.modifiers().add(generatedAnnotation);

        /* params[] field */
        VariableDeclarationFragment paramsDecl = ast.newVariableDeclarationFragment();
        paramsDecl.setName(ast.newSimpleName("params"));
        FieldDeclaration paramsFieldDecl = ast.newFieldDeclaration(paramsDecl);
        paramsFieldDecl.setType(ast.newArrayType(ast.newSimpleType(ast.newSimpleName("MemoryBlock"))));
        paramsFieldDecl.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
        paramsFieldDecl.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));
        type.bodyDeclarations().add(paramsFieldDecl);

        /* Constructor */
        MethodDeclaration cons = ast.newMethodDeclaration();
        cons.setConstructor(true);
        cons.setName(ast.newSimpleName("MemoryPreparedStatement"));
        cons.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
        SingleVariableDeclaration consParam = ast.newSingleVariableDeclaration();
        consParam.setName(ast.newSimpleName("numParams"));
        consParam.setType(ast.newPrimitiveType(PrimitiveType.INT));
        cons.parameters().add(consParam);
        Block consBody = ast.newBlock();
        Assignment paramsAssn = ast.newAssignment();
        paramsAssn.setLeftHandSide(ast.newSimpleName("params"));
        paramsAssn.setOperator(Assignment.Operator.ASSIGN);
        ArrayCreation paramsArrayCreation = ast.newArrayCreation();
        paramsArrayCreation.setType(ast.newArrayType(ast.newSimpleType(ast.newSimpleName("MemoryBlock"))));
        paramsArrayCreation.dimensions().add(ast.newSimpleName("numParams"));
        paramsAssn.setRightHandSide(paramsArrayCreation);
        consBody.statements().add(ast.newExpressionStatement(paramsAssn));
        cons.setBody(consBody);
        type.bodyDeclarations().add(cons);

        // Build enums list
        final Map<String, Method> enumMethodNames = new HashMap<String, Method>();
        for (final Method method : PreparedStatement.class.getDeclaredMethods()) {
            if (!method.getName().startsWith("set")) {
                continue;
            }
            final String mName = method.getName().substring(3);
            final StringBuilder methodName = new StringBuilder();
            methodName.append(mName.charAt(0));
            for (int i = 1; i < mName.length(); i++) {
                final char c = mName.charAt(i);
                if (Character.isUpperCase(c)) {
                    methodName.append('_').append(c);
                } else {
                    methodName.append(Character.toUpperCase(c));
                }
            }

            String enumName = methodName.toString();
            if (enumMethodNames.containsKey(enumName)) {
                int repeatCount = 2;
                while (enumMethodNames.containsKey(enumName + repeatCount)) {
                    repeatCount += 1;
                }
                enumName = enumName + repeatCount;
            }
            enumMethodNames.put(enumName, method);
        }

        /* Declare ParameterType enum */
        EnumDeclaration enumDeclaration = ast.newEnumDeclaration();
        enumDeclaration.setName(ast.newSimpleName("ParameterType"));
        for (final String enumName : enumMethodNames.keySet()) {
            EnumConstantDeclaration decl = ast.newEnumConstantDeclaration();
            decl.setName(ast.newSimpleName(enumName));
            enumDeclaration.enumConstants().add(decl);
        }
        type.bodyDeclarations().add(enumDeclaration);

        /* apply() method */
        // Blocked for the sake of organization
        {
            MethodDeclaration applyMethod = ast.newMethodDeclaration();
            applyMethod.setName(ast.newSimpleName("apply"));
            applyMethod.thrownExceptionTypes().add(ast.newSimpleType(ast.newSimpleName("SQLException")));
            SingleVariableDeclaration applyParam = ast.newSingleVariableDeclaration();
            applyParam.setType(ast.newSimpleType(ast.newSimpleName("PreparedStatement")));
            applyParam.setName(ast.newSimpleName("stmt"));
            applyParam.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));
            applyMethod.parameters().add(applyParam);
            applyParam = ast.newSingleVariableDeclaration();
            applyParam.setType(ast.newPrimitiveType(PrimitiveType.INT));
            applyParam.setName(ast.newSimpleName("originalIndex"));
            applyParam.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));
            applyMethod.parameters().add(applyParam);
            applyParam = ast.newSingleVariableDeclaration();
            applyParam.setType(ast.newPrimitiveType(PrimitiveType.INT));
            applyParam.setName(ast.newSimpleName("destIndex"));
            applyParam.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));
            applyMethod.parameters().add(applyParam);

            Block applyBody = ast.newBlock();
            VariableDeclarationFragment varDeclFrag = ast.newVariableDeclarationFragment();
            ArrayAccess arrayAcc = ast.newArrayAccess();
            arrayAcc.setArray(ast.newSimpleName("params"));
            InfixExpression infixExpression = ast.newInfixExpression();
            infixExpression.setLeftOperand(ast.newSimpleName("originalIndex"));
            infixExpression.setOperator(InfixExpression.Operator.MINUS);
            infixExpression.setRightOperand(ast.newNumberLiteral("1"));
            arrayAcc.setIndex(infixExpression);
            varDeclFrag.setInitializer(arrayAcc);
            varDeclFrag.setName(ast.newSimpleName("memoryBlock"));
            VariableDeclarationStatement varDeclStmt = ast.newVariableDeclarationStatement(varDeclFrag);
            varDeclStmt.setType(ast.newSimpleType(ast.newSimpleName("MemoryBlock")));
            applyBody.statements().add(varDeclStmt);

            MethodInvocation methodInvocation = ast.newMethodInvocation();
            methodInvocation.setExpression(ast.newSimpleName("memoryBlock"));
            methodInvocation.setName(ast.newSimpleName("getParamType"));
            SwitchStatement switchStatement = ast.newSwitchStatement();
            switchStatement.setExpression(methodInvocation);
            applyBody.statements().add(switchStatement);

            for (final Map.Entry<String, Method> entry : enumMethodNames.entrySet()) {
                final String enumName = entry.getKey();
                final Method method = entry.getValue();

                SwitchCase switchCase = ast.newSwitchCase();
                switchCase.setExpression(ast.newSimpleName(enumName));
                switchStatement.statements().add(switchCase);

                methodInvocation = ast.newMethodInvocation();
                methodInvocation.setExpression(ast.newSimpleName("stmt"));
                methodInvocation.setName(ast.newSimpleName(method.getName()));
                methodInvocation.arguments().add(ast.newSimpleName("destIndex"));
                for (int i = 1; i < method.getParameterTypes().length; i++) {
                    MethodInvocation getParam = ast.newMethodInvocation();
                    getParam.setExpression(ast.newSimpleName("memoryBlock"));
                    getParam.setName(ast.newSimpleName("getParam"));
                    getParam.arguments().add(ast.newNumberLiteral(Integer.toString(i-1)));
                    CastExpression castExpr = ast.newCastExpression();
                    castExpr.setExpression(getParam);
                    castExpr.setType(classToBoxType(ast, method.getParameterTypes()[i]));
                    methodInvocation.arguments().add(castExpr);
                }
                switchStatement.statements().add(ast.newExpressionStatement(methodInvocation));

                switchStatement.statements().add(ast.newBreakStatement());

            }
            SwitchCase switchCase = ast.newSwitchCase();
            switchCase.setExpression(null);
            switchStatement.statements().add(switchCase);
            ThrowStatement throwStatement = ast.newThrowStatement();
            ClassInstanceCreation illegalState = ast.newClassInstanceCreation();
            illegalState.setType(ast.newSimpleType(ast.newSimpleName("IllegalStateException")));
            throwStatement.setExpression(illegalState);
            switchStatement.statements().add(throwStatement);

            applyMethod.setBody(applyBody);
            type.bodyDeclarations().add(applyMethod);
        }

        /* Clear parameters method */
        {
            MethodDeclaration methodDeclaration = ast.newMethodDeclaration();
            methodDeclaration.setName(ast.newSimpleName("clearParameters"));
            Block body = ast.newBlock();

            ForStatement applyFor = ast.newForStatement();
            VariableDeclarationFragment applyForInitFrag = ast.newVariableDeclarationFragment();
            applyForInitFrag.setName(ast.newSimpleName("i"));
            applyForInitFrag.setInitializer(ast.newNumberLiteral("0"));
            VariableDeclarationExpression applyForInit = ast.newVariableDeclarationExpression(applyForInitFrag);
            applyForInit.setType(ast.newPrimitiveType(PrimitiveType.INT));
            applyFor.initializers().add(applyForInit);
            PostfixExpression applyForUpdater = ast.newPostfixExpression();
            applyForUpdater.setOperator(PostfixExpression.Operator.INCREMENT);
            applyForUpdater.setOperand(ast.newSimpleName("i"));
            applyFor.updaters().add(applyForUpdater);
            InfixExpression applyForExpr = ast.newInfixExpression();
            applyForExpr.setOperator(InfixExpression.Operator.LESS);
            applyForExpr.setLeftOperand(ast.newSimpleName("i"));
            FieldAccess paramsLength = ast.newFieldAccess();
            paramsLength.setExpression(ast.newSimpleName("params"));
            paramsLength.setName(ast.newSimpleName("length"));
            applyForExpr.setRightOperand(paramsLength);
            applyFor.setExpression(applyForExpr);

            Block forBlock = ast.newBlock();
            Assignment assignment = ast.newAssignment();
            ArrayAccess arrayAcc = ast.newArrayAccess();
            arrayAcc.setArray(ast.newSimpleName("params"));
            arrayAcc.setIndex(ast.newSimpleName("i"));
            assignment.setLeftHandSide(arrayAcc);
            assignment.setOperator(Assignment.Operator.ASSIGN);
            assignment.setRightHandSide(ast.newNullLiteral());
            forBlock.statements().add(ast.newExpressionStatement(assignment));
            applyFor.setBody(forBlock);
            body.statements().add(applyFor);
            methodDeclaration.setBody(body);
            type.bodyDeclarations().add(methodDeclaration);
        }

        /* Setter methods */

        for (Map.Entry<String, Method> entry : enumMethodNames.entrySet()) {
            final String enumName = entry.getKey();
            final Method method = entry.getValue();
            if (!method.getName().startsWith("set")) {
                continue;
            }

            MethodDeclaration methodDeclaration = ast.newMethodDeclaration();
            methodDeclaration.setConstructor(false);
            List modifiers = methodDeclaration.modifiers();
            modifiers.add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
            methodDeclaration.setName(ast.newSimpleName(method.getName()));
            if (method.getReturnType() != void.class) {
                throw new IllegalStateException();
            }
            methodDeclaration.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));

            if (method.getParameterTypes()[0] != int.class) { throw new IllegalStateException(); }
            SingleVariableDeclaration variableDeclaration = ast.newSingleVariableDeclaration();
            variableDeclaration.setType(ast.newPrimitiveType(PrimitiveType.INT));
            variableDeclaration.setName(ast.newSimpleName("parameterIndex"));
            methodDeclaration.parameters().add(variableDeclaration);

            for (int i = 1; i < method.getParameterTypes().length; i++) {
                final Class<?> paramType = method.getParameterTypes()[i];
                variableDeclaration = ast.newSingleVariableDeclaration();
                variableDeclaration.setType(classToType(ast, paramType));
                variableDeclaration.setName(ast.newSimpleName("p" + i));
                methodDeclaration.parameters().add(variableDeclaration);
            }

            methodDeclaration.thrownExceptionTypes().add(ast.newSimpleType(ast.newSimpleName("SQLException")));

            org.eclipse.jdt.core.dom.Block block = ast.newBlock();
            Assignment assignment = ast.newAssignment();
            assignment.setOperator(Assignment.Operator.ASSIGN);
            ArrayAccess lhsArray = ast.newArrayAccess();
            lhsArray.setArray(ast.newSimpleName("params"));
            InfixExpression infixExpression = ast.newInfixExpression();
            infixExpression.setLeftOperand(ast.newSimpleName("parameterIndex"));
            infixExpression.setOperator(InfixExpression.Operator.MINUS);
            infixExpression.setRightOperand(ast.newNumberLiteral("1"));
            lhsArray.setIndex(infixExpression);
            assignment.setLeftHandSide(lhsArray);
            ClassInstanceCreation rhs = ast.newClassInstanceCreation();
            rhs.setType(ast.newSimpleType(ast.newSimpleName("MemoryBlock")));
            rhs.arguments().add(ast.newQualifiedName(ast.newSimpleName("ParameterType"), ast.newSimpleName(enumName)));
            for (int i = 1; i < method.getParameterTypes().length; i++) {
                rhs.arguments().add(ast.newSimpleName("p" + i));
            }
            assignment.setRightHandSide(rhs);
            block.statements().add(ast.newExpressionStatement(assignment));
            methodDeclaration.setBody(block);
            type.bodyDeclarations().add(methodDeclaration);
        }

        unit.types().add(type);

        FileWriter fw = null;
        try {
            File packageDir = new File(outputDir, "com/coverity/security/sql");
            if (!packageDir.exists()) {
                packageDir.mkdirs();
            }

            fw = new FileWriter(new File(packageDir, "MemoryPreparedStatement.java"));
            fw.write(unit.toString());
            fw.close();
        } catch (IOException e) {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e2) { }
            }
            throw new RuntimeException(e);
        }

    }
}
