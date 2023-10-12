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
public abstract class AbstractOpExactCmp extends AbstractOpCmp {

    public AbstractOpExactCmp(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    /**
     * Returns the type of the literal obtained by the operation equals (or not
     * equals).
     * Each operand can be of type int, float or boolean.
     * 
     * @author H
     */
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type leftOpType = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type rightOpType = getRightOperand().verifyExpr(compiler, localEnv, currentClass);

        if (!leftOpType.isComparableToExact(rightOpType))
            throw new ContextualError(String.format(ContextualError.INCOMPATIBLE_TYPE_BINARY_OPERATOR,
                    rightOpType.getName(),
                    leftOpType.getName(),
                    getOperatorName()),
                    getLocation());

        setType(compiler.environmentType.BOOLEAN);
        return getType();
    }
}
