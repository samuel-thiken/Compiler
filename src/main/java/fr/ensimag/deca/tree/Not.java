package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.memory.MemoryLocation;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.instructions.ADD;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.SEQ;

/**
 *
 * @author gl20
 * @date 01/01/2023
 */
public class Not extends AbstractUnaryExpr {

    public Not(AbstractExpr operand) {
        super(operand);
    }

    /**
     * Verify if the operand is compatile with the operation Not.
     * If yes, the type of the operation is set with that of the operand (boolean).
     * If the type of the operand is incompatible, a Contextual Exception is thrown.
     * 
     * @author H
     */
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        getOperand().verifyExpr(compiler, localEnv, currentClass);
        getOperand().verifyCondition(compiler, localEnv, currentClass);
        setType(getOperand().getType());
        return getType();
    }

    @Override
    protected String getOperatorName() {
        return "!";
    }

    @Override
    public MemoryLocation.Register codeGenEvaluate(DecacCompiler compiler) {
        MemoryLocation.Register register;

        try (MemoryLocation memOperand = getOperand().codeGenEvaluate(compiler);
                MemoryLocation.Immediate one = MemoryLocation.Immediate.create(null, new ImmediateInteger(1))) {
            MemoryLocation[] locations = MemoryLocation.Register.getUnused(compiler, memOperand, null);
            register = (MemoryLocation.Register) locations[0];

            // Opération : operand + 1 == 1 ?
            // compiler.addInstruction(new LOAD(memOperand.getDVal(compiler),
            // register.getRegister(compiler))); // reg = a
            compiler.addInstruction(new ADD(one.getDVal(compiler), register.getRegister(compiler))); // reg += 1 (req = a + 1)
            compiler.addInstruction(new CMP(one.getDVal(compiler), register.getRegister(compiler))); // reg == 1 ?
            compiler.addInstruction(new SEQ(register.getRegister(compiler)));
        }

        return register;
    }
}
