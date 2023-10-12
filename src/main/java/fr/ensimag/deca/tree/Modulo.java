package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.ExecutionError;
import fr.ensimag.deca.codegen.ExecutionError.Check;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.memory.MemoryLocation;
import fr.ensimag.ima.pseudocode.instructions.REM;

/**
 *
 * @author gl20
 * @date 01/01/2023
 */
public class Modulo extends AbstractOpArith {

    public Modulo(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    /**
     * Verify if the operands are compatible with the modulo operator.
     * If yes, the modulo type is set to int. Otherwise a contextual error is
     * thrown.
     * 
     * @author H
     */
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        getRightOperand().verifyExpr(compiler, localEnv, currentClass);

        if (getLeftOperand().getType().isInt() && getRightOperand().getType().isInt()) {
            setType(compiler.environmentType.INT);
            return getType();
        }

        throw new ContextualError(String.format(ContextualError.INCOMPATIBLE_TYPE_BINARY_OPERATOR,
                getRightOperand().getType().getName(),
                getLeftOperand().getType().getName(),
                getOperatorName()),
                getLocation());
    }

    @Override
    protected String getOperatorName() {
        return "%";
    }

    @Override
    public MemoryLocation.Register codeGenEvaluate(DecacCompiler compiler) {
        MemoryLocation.Register register;

        try (MemoryLocation memLeft = getLeftOperand().codeGenEvaluate(compiler);
                MemoryLocation memRight = getRightOperand().codeGenEvaluate(compiler);) {
            MemoryLocation[] locations = MemoryLocation.Register.getUnused(compiler, memLeft, memRight, true);
            register = (MemoryLocation.Register) locations[0];

            ExecutionError.check(compiler, memRight, Check.MODULO_BY_ZERO, getLocation());

            // compiler.addInstruction(new LOAD(memLeft.getDVal(compiler),
            // register.getRegister(compiler))); // reg = a
            compiler.addInstruction(new REM(memRight.getDVal(compiler), register.getRegister(compiler))); // reg = reg %
                                                                                                          // b
        }

        return register;
    }

}
