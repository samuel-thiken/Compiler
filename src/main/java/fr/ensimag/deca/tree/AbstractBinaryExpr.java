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
import fr.ensimag.ima.pseudocode.instructions.WFLOAT;
import fr.ensimag.ima.pseudocode.instructions.WFLOATX;
import fr.ensimag.ima.pseudocode.instructions.WINT;

/**
 * Binary expressions.
 *
 * @author gl20
 * @date 01/01/2023
 */
public abstract class AbstractBinaryExpr extends AbstractExpr {

    public AbstractExpr getLeftOperand() {
        return leftOperand;
    }

    public AbstractExpr getRightOperand() {
        return rightOperand;
    }

    protected void setLeftOperand(AbstractExpr leftOperand) {
        Validate.notNull(leftOperand);
        this.leftOperand = leftOperand;
    }

    protected void setRightOperand(AbstractExpr rightOperand) {
        Validate.notNull(rightOperand);
        this.rightOperand = rightOperand;
    }

    private AbstractExpr leftOperand;
    private AbstractExpr rightOperand;

    public AbstractBinaryExpr(AbstractExpr leftOperand,
            AbstractExpr rightOperand) {
        Validate.notNull(leftOperand, "left operand cannot be null");
        Validate.notNull(rightOperand, "right operand cannot be null");
        Validate.isTrue(leftOperand != rightOperand, "Sharing subtrees is forbidden");
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
    }

    @Override
    protected void codeGenPrint(DecacCompiler compiler, boolean hex) {
        try (MemoryLocation mem = codeGenEvaluate(compiler).toRegister(compiler, 1)) {
            if (getType().isInt())
                compiler.addInstruction(new WINT());
            else
                compiler.addInstruction(hex ? new WFLOATX() : new WFLOAT());
        }
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // Nothing to do
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("(");
        getLeftOperand().decompile(s);
        s.print(" " + getOperatorName() + " ");
        getRightOperand().decompile(s);
        s.print(")");
    }

    /**
     * Returns the type of the literal obtained by the binary operation.
     * 
     * @author H
     */
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type leftOpType = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type rightOpType = getRightOperand().verifyExpr(compiler, localEnv, currentClass);
        try {
            setRightOperand(getRightOperand().verifyRValue(compiler, localEnv, currentClass, leftOpType));
        } catch(ContextualError e) {
            throw new ContextualError(String.format(ContextualError.INCOMPATIBLE_TYPE_BINARY_OPERATOR,
                    rightOpType.getName(),
                    leftOpType.getName(),
                    getOperatorName()),
                    getLocation());
        }
        setType(leftOpType);
        return getType();
    }

    abstract protected String getOperatorName();

    @Override
    protected void iterChildren(TreeFunction f) {
        leftOperand.iter(f);
        rightOperand.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        leftOperand.prettyPrint(s, prefix, false);
        rightOperand.prettyPrint(s, prefix, true);
    }

}
