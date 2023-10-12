package fr.ensimag.deca.tree;

import java.io.PrintStream;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.context.ParamDefinition;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;

/**
 * Declaration of a parameter (of a method).
 * 
 * @author H
 */
public class DeclParam extends AbstractDeclParam {

    public DeclParam(AbstractIdentifier type, AbstractIdentifier paramName) {
        super(type, paramName);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        getType().prettyPrint(s, prefix, false);
        getParamName().prettyPrint(s, prefix, true);
    }

    @Override
    protected Type verifyDeclParam(DecacCompiler compiler, EnvironmentExp envParams)
            throws ContextualError {
        
        //pass 2
        getType().verifyType(compiler, null, null);
        if (getType().getType().isVoid())
            throw new ContextualError(String.format(ContextualError.VOID_PARAM_NOT_ALLOWED, getParamName().getName()),
                    getLocation());
        ParamDefinition paramDef = new ParamDefinition(getType().getType(), getLocation());
        getParamName().setDefinition(paramDef);
        getParamName().setType(getType().getType());

        // pass 3
        try {
            envParams.declare(getParamName().getName(), paramDef);
        } catch (DoubleDefException e) {
            System.out.println(String.format(ContextualError.ALREADY_DEFINED_PARAM, getParamName().getName().toString()));
            System.exit(1);
        }

        return getType().getType();
    }

    @Override
    protected void codeGenDeclParam(DecacCompiler compiler) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(getType().decompile() + " " + getParamName().decompile());
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        getType().iter(f);
        getParamName().iter(f);
    }
}
