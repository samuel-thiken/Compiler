package fr.ensimag.deca.tree;

import java.io.PrintStream;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;

/**
 * Print statement (print, println, ...).
 *
 * @author gl20
 * @date 01/01/2023
 */
public abstract class AbstractPrint extends AbstractInst {

    private boolean printHex;
    private ListExpr arguments = new ListExpr();

    abstract String getSuffix();

    public AbstractPrint(boolean printHex, ListExpr arguments) {
        Validate.notNull(arguments);
        this.arguments = arguments;
        this.printHex = printHex;
    }

    public ListExpr getArguments() {
        return arguments;
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        for (AbstractExpr a : getArguments().getList()) {
            a.verifyExpr(compiler, localEnv, currentClass);
            if (!a.getType().isIntOrFloat() && !a.getType().isString())
                throw new ContextualError(String.format(ContextualError.INCOMPATIBLE_TYPE_PARAMETER, a.getType().getName(), "printable"), getLocation());
        }
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        compiler.addComment(String.format("Affichage de %s", getArguments().getList().stream().map(a -> a.decompile()).collect(Collectors.joining(", "))));
        for (AbstractExpr a : getArguments().getList()) {
            a.codeGenPrint(compiler, getPrintHex());
        }
    }

    private boolean getPrintHex() {
        return printHex;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("print" + getSuffix() + (getPrintHex() ? "x" : "") + "(");
        getArguments().decompile(s);
        s.print(")");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        arguments.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        arguments.prettyPrint(s, prefix, true);
    }

}
