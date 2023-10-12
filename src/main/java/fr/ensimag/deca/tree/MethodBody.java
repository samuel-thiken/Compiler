package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;

public class MethodBody extends AbstractMethodBody {
    private ListDeclVar declVariables;
    private ListInst insts;

    public MethodBody(ListDeclVar declVariables, ListInst insts) {
        Validate.notNull(declVariables);
        Validate.notNull(insts);
        this.declVariables = declVariables;
        this.insts = insts;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("{");
        s.indent();
        s.println();
        declVariables.decompile(s);
        insts.decompile(s);
        s.unindent();
        s.print("}");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        declVariables.prettyPrint(s, prefix, false);
        insts.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        declVariables.iter(f);
        insts.iter(f);
    }

    @Override
    public void codeGenMethodBody(DecacCompiler compiler) {
        declVariables.codeGenListDeclVar(compiler);
        insts.codeGenListInst(compiler);
    }

    @Override
    public void verifyMethodBody(DecacCompiler compiler, EnvironmentExp envClass, EnvironmentExp envParams,
            ClassDefinition currentClass, Type returnType) throws ContextualError {
        declVariables.verifyListDeclVariable(compiler, envClass, envParams, currentClass);
        insts.verifyListInst(compiler, envParams, currentClass, returnType);
    }
}
