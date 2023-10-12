package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.memory.MemoryLocation;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.instructions.WINT;

import java.io.PrintStream;

/**
 * Integer literal
 *
 * @author gl20
 * @date 01/01/2023
 */
public class IntLiteral extends AbstractLiteral {
    public int getValue() {
        return value;
    }

    private int value;

    @Override
    public String getStringRepresentation() {
        return Integer.toString(value);
    }

    public IntLiteral(int value) {
        this.value = value;

        setMemoryLocation(MemoryLocation.Immediate.create(this, new ImmediateInteger(value)));
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        setType(compiler.environmentType.INT);
        return getType();
    }


    @Override
    String prettyPrintNode() {
        return "Int (" + getValue() + ")";
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(Integer.toString(value));
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
    protected void codeGenPrint(DecacCompiler compiler, boolean hex) {
        try (MemoryLocation register = codeGenEvaluate(compiler).toRegister(compiler, 1);) {
            compiler.addInstruction(new WINT());
        }
    }

}
