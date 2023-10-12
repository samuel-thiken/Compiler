package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.memory.MemoryLocation;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.SNE;

/**
 *
 * @author gl20
 * @date 01/01/2023
 */
public class NotEquals extends AbstractOpExactCmp {

    public NotEquals(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected String getOperatorName() {
        return "!=";
    }

    @Override
    public MemoryLocation codeGenEvaluate(DecacCompiler compiler) {
        MemoryLocation.Register register;

        try (MemoryLocation memLeft = getLeftOperand().codeGenEvaluate(compiler);
                MemoryLocation memRight = getRightOperand().codeGenEvaluate(compiler);) {
            MemoryLocation[] locations = MemoryLocation.Register.getUnused(compiler, memLeft, memRight);
            register = (MemoryLocation.Register) locations[0];

            // compiler.addInstruction(new LOAD(memRight.getDVal(compiler),
            // register.getRegister(compiler)));
            compiler.addInstruction(new CMP(locations[1].getDVal(compiler), register.getRegister(compiler)));
            compiler.addInstruction(new SNE(register.getRegister(compiler)));
        }

        return register;
    }

}
