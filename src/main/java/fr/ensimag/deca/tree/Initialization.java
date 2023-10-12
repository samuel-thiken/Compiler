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
import fr.ensimag.ima.pseudocode.instructions.STORE;

/**
 * @author gl20
 * @date 01/01/2023
 */
public class Initialization extends AbstractInitialization {

    public AbstractExpr getExpression() {
        return expression;
    }

    private AbstractExpr expression;

    public void setExpression(AbstractExpr expression) {
        Validate.notNull(expression);
        this.expression = expression;
    }

    public Initialization(AbstractExpr expression) {
        Validate.notNull(expression);
        this.expression = expression;
    }

    @Override
    protected void verifyInitialization(DecacCompiler compiler, Type t,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        expression = expression.verifyRValue(compiler, localEnv, currentClass, t);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        expression.decompile(s);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        expression.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        expression.prettyPrint(s, prefix, true);
    }

    @Override
    protected MemoryLocation.Register codeGenInitialization(DecacCompiler compiler, MemoryLocation.Address addr,
            Integer tempRegisterIndex, boolean disableStore) {
        MemoryLocation.Register register;
        if (expression instanceof ArrayExpr) {
            register = ((ArrayExpr) expression).codeGenInit(compiler, addr, tempRegisterIndex);
        }
        else {
            if (tempRegisterIndex == null)
                register = expression.codeGenEvaluate(compiler).toRegister(compiler);
            else
                register = expression.codeGenEvaluate(compiler).toRegister(compiler, tempRegisterIndex);
            compiler.addInstruction(new STORE(register.getRegister(compiler), addr.getRegisterOffset(compiler)));
        }
        return register;
    }
}
