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

public class This extends AbstractExpr {
    private boolean value;

    public This(boolean val) {
        Validate.notNull(val);
        this.value = val;
    }

    public boolean getValue() {
        return this.value;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        if (currentClass == null)
            throw new ContextualError(String.format(ContextualError.CANNOT_USE_THIS_OUTSIDE_CLASS), getLocation());
        setType(currentClass.getType());
        return currentClass.getType();
    }

    @Override
    public void decompile(IndentPrintStream s) {
        if(!value)
            s.print("this");
    }

    @Override
    String prettyPrintNode() {
        return "This";
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node -> nothing to do
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node -> nothing to do
    }

    @Override
    public MemoryLocation codeGenEvaluate(DecacCompiler compiler) {
        return compiler.getMemoryManager().getVarExpression(compiler.createSymbol("this")).getMemoryLocation(compiler).use();
    }

}
