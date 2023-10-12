package fr.ensimag.deca.tree;

import java.io.PrintStream;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;

public class EmptyExpr extends AbstractExpr {
    
    public EmptyExpr() {}

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        setType(compiler.environmentType.INT);
        return getType();
    }

    @Override
    public void decompile(IndentPrintStream s) {
        // TODO Auto-generated method stub
        
    }


    @Override
    protected void prettyPrintType(PrintStream s, String prefix) {
        //
    }
    @Override
    String prettyPrintNode() {
        return "EmptyExpr";
    }
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        //
        
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // TODO Auto-generated method stub
        
    }
    
}
