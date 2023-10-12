package fr.ensimag.deca.tree;

import java.io.PrintStream;
import java.util.Arrays;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.ExecutionError;
import fr.ensimag.deca.codegen.ExecutionError.Check;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.memory.MemoryLocation;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.ADDSP;
import fr.ensimag.ima.pseudocode.instructions.BSR;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.ima.pseudocode.instructions.SUBSP;
import fr.ensimag.ima.pseudocode.instructions.TSTO;

public class MethodCall extends AbstractExpr {

    final private AbstractExpr target;
    final private AbstractIdentifier methodName;
    final private ListExpr params;

    public MethodCall(AbstractExpr target, AbstractIdentifier methodName, ListExpr params) {
        Validate.notNull(target);
        Validate.notNull(methodName);
        Validate.notNull(params);
        this.target = target;
        this.methodName = methodName;
        this.params = params;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        target.decompile(s);
        if(((target instanceof This) && !((This) target).getValue()) || !(target instanceof This)) {
            s.print(".");
        }
        methodName.decompile(s);
        s.print("(");
        params.decompile(s);
        s.print(")");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        target.prettyPrint(s, prefix, false);
        methodName.prettyPrint(s, prefix, false);
        params.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        target.iter(f);
        methodName.iter(f);
        params.iter(f);
    }

    /*
     * @author H
     */
    private ClassDefinition isMethodInherited(ClassDefinition methodClassDef) {
        ClassDefinition classDef = methodClassDef;
        while (classDef.getSuperClass() != null) {
            if (classDef.getMembers().has(methodName.getName()))
                return classDef;
            classDef = classDef.getSuperClass();
        }
        return null;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type typeObject;
        try { // we have to catch the exception in case the target is a 'this' keyword
              // (implicitly or explicitly called)
            typeObject = target.verifyExpr(compiler, localEnv, currentClass);
        } catch (ContextualError e) {
            if (target.prettyPrintNode().equals("This"))
                throw new ContextualError(String.format(ContextualError.INVALID_METHOD_CALL), getLocation());
            throw e;
        }
        // verify if the type's target (the object which the method is being called on)
        if (!typeObject.isClass()) // can be void or class type
            throw new ContextualError(String.format(ContextualError.INVALID_METHOD_CALL), getLocation());

        // verify if the class in which the method
        if (!compiler.environmentType.has(typeObject.getName()))
            throw new ContextualError(String.format(ContextualError.UNDEFINED_TYPE, typeObject.getName()),
                    getLocation());

        ClassDefinition classMethodCalledOn = (ClassDefinition) compiler.environmentType
                .defOfType(typeObject.getName());
        ClassDefinition classDefMethod = isMethodInherited(classMethodCalledOn); // class in which the method is defined

        // verify if the method is defined in superclass of the class on which the
        // method is called on
        if (classDefMethod == null)
            throw new ContextualError(String.format(ContextualError.UNDEFINED_METHOD, methodName.getName()),
                    getLocation());

        // verify if the identifier of nameMethod is a method ident
        Definition def = classDefMethod.getMembers().get(methodName.getName());
        methodName.verifyExpr(compiler, classDefMethod.getMembers(), currentClass);

        if (!def.isMethod())
            throw new ContextualError(
                    String.format(ContextualError.UNDEFINED_METHOD, methodName.getName()),
                    getLocation());

        MethodDefinition methodDef = (MethodDefinition) def;
        Signature methodSignature = methodDef.getSignature();
        Type returnTypeMethod = methodDef.getType();

        // checking if the size's signature is the same as the list of entries
        if (params.size() != methodSignature.size())
            throw new ContextualError(String.format(ContextualError.INVALID_NB_PARAMETERS, methodName.getName()),
                    getLocation());

        // checking if all entries have the same type as the parameter in the signature
        // at the same index
        for (int i = 0; i < params.size(); i++) {
            Type paramType = methodSignature.paramNumber(i);
            AbstractExpr currentEntry = params.getList().get(i);
            params.getModifiableList().set(i, currentEntry.verifyRValue(compiler, localEnv, currentClass, paramType));
        }

        setType(returnTypeMethod);
        methodName.setDefinition(def);
        return returnTypeMethod;
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        MemoryLocation register = codeGenEvaluate(compiler);
        register.close();
    }

    @Override
    public MemoryLocation codeGenEvaluate(DecacCompiler compiler) {
        compiler.getMemoryManager().save();

        // On empile les paramètres
        compiler.addInstruction(new TSTO(new ImmediateInteger(params.size() + 1 + 2)));
        ExecutionError.check(compiler, null, Check.STACK_OVERFLOW, getLocation());
        compiler.addInstruction(new ADDSP(new ImmediateInteger(params.size() + 1)));

        int spIndex = 0;

        MemoryLocation object = target.codeGenEvaluate(compiler);
        MemoryLocation.Register objectRegister = object.toRegister(compiler);
        compiler.addInstruction(new STORE(objectRegister.getRegister(compiler), new RegisterOffset(spIndex--, Register.SP)));
        // objectRegister.close();

        for (AbstractExpr expr : params.getList()) {
            MemoryLocation param = expr.codeGenEvaluate(compiler);
            MemoryLocation.Register paramRegister = param.toRegister(compiler);

            compiler.addInstruction(
                    new STORE(paramRegister.getRegister(compiler), new RegisterOffset(spIndex--, Register.SP)));

            paramRegister.close();
        }

        // On jump à la méthode
        compiler.addInstruction(new LOAD(new RegisterOffset(0, objectRegister.getRegister(compiler)), Register.R1));
        compiler.addInstruction(new BSR(new RegisterOffset(methodName.getMethodDefinition().getIndex(), Register.R1)));

        // On dépile les paramètres
        compiler.addInstruction(new SUBSP(new ImmediateInteger(params.size() + 1)));

        compiler.getMemoryManager().restore(Arrays.asList(new Integer[] {0}));

        return MemoryLocation.Register.create(compiler, 0);
    }

}