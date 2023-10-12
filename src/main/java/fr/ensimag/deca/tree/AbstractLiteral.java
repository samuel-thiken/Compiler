package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.memory.MemoryLocation;

public abstract class AbstractLiteral extends AbstractExpr {

    @Override
    public MemoryLocation codeGenEvaluate(DecacCompiler compiler) {
        return getMemoryLocation(compiler);
    }

    public abstract String getStringRepresentation();

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // Nothing to do
    }
    
}
