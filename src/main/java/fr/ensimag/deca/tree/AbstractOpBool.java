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
public abstract class AbstractOpBool extends AbstractBinaryExpr {

    public AbstractOpBool(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    /**
     * Returns the type of the literal obtained by the binary operation.
     * 
     * @author H
     */
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        getRightOperand().verifyExpr(compiler, localEnv, currentClass);

        if (getLeftOperand() instanceof AbstractIdentifier && !getLeftOperand().getType().isBoolean())
            throw new ContextualError(String.format(ContextualError.INCOMPATIBLE_TYPE_BINARY_OPERATOR,
                    getLeftOperand().getType().getName(), getRightOperand().getType().getName(), getOperatorName()),
                    getLocation());

        if (getRightOperand() instanceof AbstractIdentifier && !getRightOperand().getType().isBoolean())
            throw new ContextualError(String.format(ContextualError.INCOMPATIBLE_TYPE_BINARY_OPERATOR,
                    getRightOperand().getType().getName(), getLeftOperand().getType().getName(), getOperatorName()),
                    getLocation());
        // we verify that both operands are boolean conditions
        getLeftOperand().verifyCondition(compiler, localEnv, currentClass);
        getRightOperand().verifyCondition(compiler, localEnv, currentClass);

        // the type of the operation is boolean
        setType(compiler.environmentType.BOOLEAN);
        return getType();
    }

}
