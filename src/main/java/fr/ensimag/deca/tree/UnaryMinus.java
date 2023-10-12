package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.memory.MemoryLocation;
import fr.ensimag.ima.pseudocode.instructions.OPP;

/**
 * @author gl20
 * @date 01/01/2023
 */
public class UnaryMinus extends AbstractUnaryExpr {

    public UnaryMinus(AbstractExpr operand) {
        super(operand);
    }

    /**
     * Verify if the operand is compatile with the operation Unary Minus.
     * If yes, the type of the operation is set with that of the operand (int or
     * float).
     * If the type of the operand is incompatible, a Contextual Exception is thrown.
     * 
     * @author H
     */
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        getOperand().verifyExpr(compiler, localEnv, currentClass);
        Type operandType = getOperand().getType();
        if (!operandType.isIntOrFloat())
            throw new ContextualError(String.format(ContextualError.INCOMPATIBLE_TYPE_UN_OPERATOR,
                    getOperand().getType().getName(), getOperatorName()), getLocation());
        setType(operandType);
        return operandType;
    }

    @Override
    protected String getOperatorName() {
        return "-";
    }

    @Override
    public MemoryLocation.Register codeGenEvaluate(DecacCompiler compiler) {
        MemoryLocation.Register register;

        compiler.addComment(String.format("Inversion de %s", getOperand().decompile()));
        try (MemoryLocation memOperand = getOperand().codeGenEvaluate(compiler)) {
            MemoryLocation[] locations = MemoryLocation.Register.getUnused(compiler, memOperand, null);
            register = (MemoryLocation.Register) locations[0];

            compiler.addComment(String.format("Opération inversion de %s", getOperand().decompile()));
            compiler.addInstruction(new OPP(memOperand.getDVal(compiler), register.getRegister(compiler)));
        }

        return register;
    }

}
