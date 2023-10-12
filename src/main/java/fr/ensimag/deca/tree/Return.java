package fr.ensimag.deca.tree;

import java.io.PrintStream;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.memory.MemoryLocation;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.instructions.RTS;

public class Return extends AbstractInst {
    private AbstractExpr expression;

    public Return(AbstractExpr expression) {
        this.expression = expression;
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass,
            Type returnType) throws ContextualError {
        if (returnType.isVoid())
            throw new ContextualError(String.format(ContextualError.NO_RETURN_IN_VOID_METHOD), getLocation());
        expression = expression.verifyRValue(compiler, localEnv, currentClass, returnType);
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        try (MemoryLocation register = expression.codeGenEvaluate(compiler).toRegister(compiler, 0)) {
            // Restoration des registres
            compiler.getMemoryManager().restore(null);
            // Return
            compiler.addInstruction(new RTS());
        }
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("return ");
        expression.decompile(s);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        expression.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        expression.iter(f);
    }

}
