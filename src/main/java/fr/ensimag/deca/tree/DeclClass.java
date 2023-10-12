package fr.ensimag.deca.tree;

import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.memory.MemoryLocation;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.LabelOperand;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.ADDSP;
import fr.ensimag.ima.pseudocode.instructions.BSR;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.RTS;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.ima.pseudocode.instructions.SUBSP;

/**
 * Declaration of a class (<code>class name extends superClass {members}<code>).
 * 
 * @author gl20
 * @date 01/01/2023
 */
public class DeclClass extends AbstractDeclClass {

    private AbstractIdentifier className;
    private AbstractIdentifier superClass;
    private ListDeclField fields;
    private ListDeclMethod methods;

    public DeclClass(AbstractIdentifier className, AbstractIdentifier superClass, ListDeclField fields,
            ListDeclMethod methods) {
        Validate.notNull(className);
        Validate.notNull(superClass);
        Validate.notNull(fields);
        Validate.notNull(methods);
        this.className = className;
        if (superClass.getName().getName().equals("0"))
            this.superClass = null;
        else
            this.superClass = superClass;
        this.fields = fields;
        this.methods = methods;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        if (className.getName().toString().equals("Object"))
            return;

        s.print("class " + className.decompile() + " ");
        if (superClass != null)
            s.print("extends " + superClass.decompile() + " ");
        s.print("{");
        s.println();
        s.indent();
        fields.decompile(s);
        methods.decompile(s);
        s.unindent();
        s.print("}");
    }

    /**
     * Verifies the classes' name and the class hierarchy.
     * 
     * @author H
     */
    @Override
    protected void verifyClass(DecacCompiler compiler) throws ContextualError {

        ClassType classType;
        if (className.getName().equals(compiler.environmentType.OBJECT.getName())) {
            classType = compiler.environmentType.OBJECT;
            superClass = null;
        } else {
            // verify that the class is defined in the environment type
            superClass.verifyType(compiler, null, null);
            if (!superClass.getType().isClass())
                throw new ContextualError(String.format(ContextualError.INCOMPATIBLE_TYPE_EXTENDS, className.getName(),
                        superClass.getType().getName()), getLocation());

            // defining new class' type
            classType = new ClassType(className.getName(), className.getLocation(),
                    superClass.getClassDefinition());
        }

        // checking that the current class isn't already defined in the environment type
        if (compiler.environmentType.has(className.getName()))
            throw new ContextualError(String.format(ContextualError.ALREADY_DEFINED_CLASS, className.getName()),
                    getLocation());

        ClassDefinition newClassDef = new ClassDefinition(classType, className.getLocation(),
                superClass != null ? superClass.getClassDefinition() : null);
        classType.setDefinition(newClassDef);
        newClassDef.setExpression(className);

        compiler.environmentType.add(className.getName(), newClassDef);
        className.setDefinition(newClassDef);
        className.verifyType(compiler, null, null);
    }

    /**
     * Verifies the fields and methods of a class without looking at
     * fields' initialization and method body.
     * 
     * @author H
     */
    @Override
    protected void verifyClassMembers(DecacCompiler compiler)
            throws ContextualError {
        // getting the class definition associated with the superclass
        ClassDefinition superClassDef = superClass != null ? superClass.getClassDefinition() : null;

        fields.verifyListDeclField(compiler, className.getClassDefinition().getMembers(), className, superClass);
        methods.verifyListDeclMethod(compiler, className.getClassDefinition().getMembers(), className, superClass);

        EnvironmentExp superEnv = superClass != null ? superClassDef.getMembers() : null;

        className.getClassDefinition().getMembers().setParentEnvironment(superEnv);
    }

    @Override
    protected void verifyClassBody(DecacCompiler compiler) throws ContextualError {
        
        fields.verifyListDeclFieldInit(compiler, className.getClassDefinition().getMembers(), className, superClass);
        methods.verifyListDeclMethodBody(compiler, className.getClassDefinition().getMembers(), className, superClass);

        if (!compiler.environmentType.has(className.getName()))
            throw new ContextualError(String.format(ContextualError.UNDEFINED_TYPE, className.getName()),
                    getLocation());
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        className.prettyPrint(s, prefix, false);
        if (superClass != null)
            superClass.prettyPrint(s, prefix, false);
        fields.prettyPrint(s, prefix, false);
        methods.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        className.iter(f);
        if (superClass != null)
            superClass.iter(f);
        fields.iter(f);
        methods.iter(f);
    }

    private void codeGenClassTableMethods(DecacCompiler compiler) {
        RegisterOffset addr = compiler.getMemoryManager().getNewStackRegisterOffset();

        // Référence vers la super classe
        className.setMemoryLocation(MemoryLocation.Address.create(className, addr));
        compiler.getMemoryManager().setVarExpression(className.getName(), className);
        if (superClass != null) {
            // On stocke l'adresse de la super class
            DAddr superAddr = superClass.getMemoryLocation(compiler).getAddress().getRegisterOffset(compiler);
            compiler.addInstruction(new LEA(superAddr, Register.R0));
        } else {
            // On met null
            DVal superAddr = new NullOperand();
            compiler.addInstruction(new LOAD(superAddr, Register.R0));
        }
        compiler.addInstruction(new STORE(Register.R0, addr));

        // Liste des méthodes
        ClassDefinition thisDef = (ClassDefinition) compiler.environmentType.defOfType(className.getName());
        List<ClassDefinition.MethodRedefinition> myMethods = thisDef.getClassMethods(compiler).stream()
                .sorted((a, b) -> a.methodDef.getIndex() - b.methodDef.getIndex()).collect(Collectors.toList());
        className.setDefinition(thisDef);

        for (ClassDefinition.MethodRedefinition method : myMethods) {
            RegisterOffset methodAddr = compiler.getMemoryManager().getNewStackRegisterOffset();

            String etiquette = String.format("code.%s.%s", method.className.getName(), method.methodName.getName());
            // On stocke l'étiquette correspondante
            ClassDefinition methodOwnerDef = (ClassDefinition) compiler.environmentType.defOfType(method.className);
            MethodDefinition methodDef = (MethodDefinition) methodOwnerDef.getMembers().get(method.methodName);
            methodDef.setLabel(new Label(etiquette));

            compiler.addInstruction(new LOAD(new LabelOperand(new Label(etiquette)), Register.R0));
            compiler.addInstruction(new STORE(Register.R0, methodAddr));
        }
    }

    private void codeGenClassTableFields(DecacCompiler compiler) {
        compiler.addLabel(new Label(String.format("init.%s", className.getName())));
        compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), Register.R1));

        if (superClass != null) {
            compiler.addInstruction(new ADDSP(new ImmediateInteger(1)));
            compiler.addInstruction(new STORE(Register.R1, new RegisterOffset(0, Register.SP)));
            compiler.addInstruction(new BSR(new Label(String.format("init.%s", superClass.getName()))));
            compiler.addInstruction(new SUBSP(new ImmediateInteger(1)));
        }

        for (AbstractDeclField field : fields.getList()) {
            if (field.getFieldName().getFieldDefinition().getType().isClass()) {
                field.codeGenDeclField(compiler, true);
                compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), Register.R1));
                compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(field.getFieldName().getFieldDefinition().getIndex(), Register.R1)));
            } else {
                field.codeGenDeclField(compiler, false);
            }
            compiler.getMemoryManager().setVarExpression(field.getFieldName().getName(), field.getFieldName());
        }

        // register.close();

        compiler.addInstruction(new RTS());
    }

    @Override
    public void codeGenClassTable(DecacCompiler compiler) {
        compiler.addComment(String.format("Code de la table des méthodes de %s", className.getName()));

        codeGenClassTableMethods(compiler);
    }

    @Override
    public void codeGenClassContent(DecacCompiler compiler) {
        codeGenClassTableFields(compiler);
        for (AbstractDeclMethod declMethod : methods.getList()) {
            declMethod.codeGenDeclMethod(compiler, className.getClassDefinition());
        }
    }

}
