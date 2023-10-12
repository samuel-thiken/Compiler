package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;

/**
 *
 * @author gl20
 * @date 01/01/2023
 */
public abstract class AbstractOpIneq extends AbstractOpCmp {

    public AbstractOpIneq(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    /**
     * Returns the type of the literal obtained by the binary operation
     * (comparison).
     * 
     * @author H
     */
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type leftOpType = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type rightOpType = getRightOperand().verifyExpr(compiler, localEnv, currentClass);

        if (!leftOpType.isIntOrFloat() || !rightOpType.isIntOrFloat())
            throw new ContextualError(String.format(ContextualError.INCOMPATIBLE_TYPE_BINARY_OPERATOR,
                    rightOpType.getName(),
                    leftOpType.getName(),
                    getOperatorName()),
                    getLocation());

        if (leftOpType.isFloat() && rightOpType.isInt()) {
            setRightOperand(getRightOperand().verifyRValue(compiler, localEnv, currentClass, leftOpType));
        } else if (leftOpType.isInt() && rightOpType.isFloat()) {
            setLeftOperand(getLeftOperand().verifyRValue(compiler, localEnv, currentClass, rightOpType));
        }

        setType(compiler.environmentType.BOOLEAN);
        return getType();
    }

}
