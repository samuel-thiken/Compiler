package fr.ensimag.deca.tree;

import java.io.PrintStream;
import java.util.Arrays;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.InlinePortion;

public class MethodAsmBody extends AbstractMethodBody {

    private StringLiteral inst;

    public MethodAsmBody(StringLiteral inst) {
        Validate.notNull(inst);
        this.inst = inst;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(" asm (");
        inst.decompile(s);
        s.print(");");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        Arrays.asList(inst.prettyPrint().split("\n")).stream().map(l -> prefix + l).forEach(l -> s.println(l));
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        inst.iter(f);
    }

    @Override
    public void verifyMethodBody(DecacCompiler compiler, EnvironmentExp envClass, EnvironmentExp envParams,
            ClassDefinition currentClass, Type returnType) throws ContextualError {
        inst.verifyExpr(compiler, envParams, currentClass);
    }

    @Override
    public void codeGenMethodBody(DecacCompiler compiler) {
        compiler.addComment("Method ASM body - start");
        compiler.add(new InlinePortion(inst.getValue()));
        compiler.addComment("Method ASM body - end");
    }
}
