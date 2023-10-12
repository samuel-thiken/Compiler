package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;

public class InstanceOf extends AbstractExpr {

    private AbstractExpr expression;
    private AbstractIdentifier type;

    public InstanceOf(AbstractExpr expr, AbstractIdentifier type) {
        Validate.notNull(expr);
        Validate.notNull(type);
        this.expression = expr;
        this.type = type;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        expression.verifyExpr(compiler, localEnv, currentClass);
        type.verifyType(compiler, localEnv, currentClass);
        if (!expression.getType().isClassOrNull())
            throw new ContextualError(
                    String.format(ContextualError.INCOMPATIBLE_INSTANCE_OF, expression.getType().getName()),
                    getLocation());
        if (!type.getType().isClassOrNull())
            throw new ContextualError(String.format(ContextualError.INCOMPATIBLE_INSTANCE_OF, type.getType().getName()),
                    getLocation());
        setType(compiler.environmentType.BOOLEAN);
        return getType();
    }

    @Override
    public void decompile(IndentPrintStream s) {
        expression.decompile(s);
        s.print(" instanceof ");
        type.decompile(s);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        expression.prettyPrint(s, prefix, false);
        type.prettyPrint(s, prefix, true);

    }

    @Override
    protected void iterChildren(TreeFunction f) {
        expression.iter(f);
        type.iter(f);
    }

}
