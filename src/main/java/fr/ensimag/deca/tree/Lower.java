package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.memory.MemoryLocation;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.SGT;

/**
 *
 * @author gl20
 * @date 01/01/2023
 */
public class Lower extends AbstractOpIneq {

    public Lower(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected String getOperatorName() {
        return "<";
    }

    @Override
    public MemoryLocation.Register codeGenEvaluate(DecacCompiler compiler) {
        MemoryLocation.Register register;

        try (MemoryLocation memLeft = getLeftOperand().codeGenEvaluate(compiler);
                MemoryLocation.Register memRight = getRightOperand().codeGenEvaluate(compiler).toRegister(compiler);) {
            MemoryLocation[] locations = MemoryLocation.Register.getUnused(compiler, memLeft, memRight);
            register = (MemoryLocation.Register) locations[0];

            compiler.addInstruction(new CMP(memLeft.getDVal(compiler), memRight.getRegister(compiler)));
            compiler.addInstruction(new SGT(register.getRegister(compiler))); // inversé
        }

        return register;
    }

}
