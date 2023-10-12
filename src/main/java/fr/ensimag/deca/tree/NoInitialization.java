package fr.ensimag.deca.tree;

import java.io.PrintStream;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.memory.MemoryLocation;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.ImmediateFloat;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;

/**
 * Absence of initialization (e.g. "int x;" as opposed to "int x =
 * 42;").
 *
 * @author gl20
 * @date 01/01/2023
 */
public class NoInitialization extends AbstractInitialization {

    private Type type;

    @Override
    protected void verifyInitialization(DecacCompiler compiler, Type t,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        this.type = t;
    }


    /**
     * Node contains no real information, nothing to check.
     */
    @Override
    protected void checkLocation() {
        // nothing
    }

    @Override
    public void decompile(IndentPrintStream s) {
        // nothing
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

    @Override
    protected MemoryLocation.Register codeGenInitialization(DecacCompiler compiler, MemoryLocation.Address addr, Integer tempRegisterIndex, boolean disableStore) {
        if (type.isBoolean() || type.isInt()) compiler.addInstruction(new LOAD(new ImmediateInteger(0), Register.R0));
        else if (type.isFloat()) compiler.addInstruction(new LOAD(new ImmediateFloat(0), Register.R0));
        else if (type.isClass()) compiler.addInstruction(new LOAD(new NullOperand(), Register.R0));
        else return null;

        if (!disableStore) compiler.addInstruction(new STORE(Register.R0, addr.getRegisterOffset(compiler)));

        return null;
    }

}
