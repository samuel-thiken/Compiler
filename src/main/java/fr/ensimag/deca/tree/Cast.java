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
import fr.ensimag.ima.pseudocode.instructions.FLOAT;
import fr.ensimag.ima.pseudocode.instructions.INT;

public class Cast extends AbstractExpr {

    private AbstractIdentifier type;
    private AbstractExpr expression;

    public Cast(AbstractIdentifier t, AbstractExpr expr) {
        Validate.notNull(t);
        Validate.notNull(expr);
        this.type = t;
        this.expression = expr;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        verifyCast(compiler, localEnv, currentClass);
        setType(type.getType());
        return type.getType();
    }

    private void verifyCast(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type typeExpr = expression.verifyExpr(compiler, localEnv, currentClass);
        type.verifyType(compiler, null, null);

        if (!type.getType().isCastCompatible(typeExpr))
            throw new ContextualError(
                    String.format(ContextualError.INVALID_CAST, typeExpr.getName(), type.getName()),
                    getLocation());
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("(");
        type.decompile(s);
        s.print(") (");
        expression.decompile(s);
        s.print(")");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        expression.prettyPrint(s, prefix, true);

    }

    @Override
    protected void iterChildren(TreeFunction f) {
        expression.iter(f);
        type.iter(f);
    }

    @Override
    public MemoryLocation codeGenEvaluate(DecacCompiler compiler) {
        MemoryLocation.Register register;
        if (type.getType().isInt() && expression.getType().isFloat()) {
            // Float to int
            try (MemoryLocation memOperand = expression.codeGenEvaluate(compiler);) {
                MemoryLocation[] locations = MemoryLocation.Register.getUnused(compiler, memOperand, null);
                register = (MemoryLocation.Register) locations[0];

                compiler.addInstruction(new INT(register.getDVal(compiler), register.getRegister(compiler)));
            }
        } else if (type.getType().isFloat() && expression.getType().isInt()) {
            // Int to float
            try (MemoryLocation memOperand = expression.codeGenEvaluate(compiler);) {
                MemoryLocation[] locations = MemoryLocation.Register.getUnused(compiler, memOperand, null);
                register = (MemoryLocation.Register) locations[0];

                compiler.addInstruction(new FLOAT(register.getDVal(compiler), register.getRegister(compiler)));
            }
        } else
            // TODO Ajouter le test de Invalid Class Cast Exception
            return expression.codeGenEvaluate(compiler);

        return register;
    }

}
