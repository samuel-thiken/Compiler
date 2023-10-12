package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.memory.MemoryLocation;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.ADDSP;
import fr.ensimag.ima.pseudocode.instructions.BSR;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.instructions.NEW;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.ima.pseudocode.instructions.SUBSP;

public class New extends AbstractExpr {

    final private AbstractIdentifier typeClass;

    public New(AbstractIdentifier typeClass) {
        Validate.notNull(typeClass);
        this.typeClass = typeClass;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        typeClass.verifyType(compiler, localEnv, currentClass);
        if (!typeClass.getType().isClass())
            throw new ContextualError(String.format(ContextualError.NOT_A_TYPE_CLASS, typeClass.getName()),
                    getLocation());
        setType(typeClass.getType());
        return getType();
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("new ");
        typeClass.decompile(s);
        s.print("()");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        typeClass.prettyPrint(s, prefix, true);

    }

    @Override
    protected void iterChildren(TreeFunction f) {
        typeClass.iter(f);
    }

    @Override
    public MemoryLocation codeGenEvaluate(DecacCompiler compiler) {
        // Allouer dans le tas
        int fieldsCount = typeClass.getClassDefinition().getTotalNumberOfFields() + 1;
        MemoryLocation.Register register = MemoryLocation.Register.create(compiler);
        compiler.addInstruction(new NEW(new ImmediateInteger(fieldsCount), register.getRegister(compiler)));

        // Adresse de la base
        compiler.addInstruction(new LEA(typeClass.getClassDefinition().getExpression().getMemoryLocation(compiler)
                .getAddress().getRegisterOffset(compiler), Register.R1));
        compiler.addInstruction(new STORE(Register.R1, new RegisterOffset(0, register.getRegister(compiler))));

        // Valeurs des champs
        compiler.getMemoryManager().save();
        compiler.addInstruction(new ADDSP(new ImmediateInteger(1)));
        compiler.addInstruction(new STORE(register.getRegister(compiler), new RegisterOffset(0, Register.SP)));
        compiler.addInstruction(new BSR(new Label(String.format("init.%s", typeClass.getName()))));
        compiler.addInstruction(new SUBSP(new ImmediateInteger(1)));
        compiler.getMemoryManager().restore(null);

        // Renvoie de l'adresse
        return register;
    }

}
