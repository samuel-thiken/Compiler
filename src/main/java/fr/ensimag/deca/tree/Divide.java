package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.ExecutionError;
import fr.ensimag.deca.codegen.ExecutionError.Check;
import fr.ensimag.deca.memory.MemoryLocation;
import fr.ensimag.ima.pseudocode.instructions.DIV;
import fr.ensimag.ima.pseudocode.instructions.QUO;

/**
 *
 * @author gl20
 * @date 01/01/2023
 */
public class Divide extends AbstractOpArith {
    public Divide(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected String getOperatorName() {
        return "/";
    }

    @Override
    public MemoryLocation.Register codeGenEvaluate(DecacCompiler compiler) {
        MemoryLocation.Register register;

        compiler.addComment(
                String.format("Division de %s par %s", getLeftOperand().decompile(), getRightOperand().decompile()));

        try (MemoryLocation memLeft = getLeftOperand().codeGenEvaluate(compiler);
                MemoryLocation memRight = getRightOperand().codeGenEvaluate(compiler);) {
            MemoryLocation[] locations = MemoryLocation.Register.getUnused(compiler, memLeft, memRight, true);
            register = (MemoryLocation.Register) locations[0];

            compiler.addComment(String.format("Opération division de %s par %s", getLeftOperand().decompile(),
                    getRightOperand().decompile()));

            ExecutionError.check(compiler, memRight,
                    getRightOperand().getType().isInt() ? Check.DIVISION_INT_BY_ZERO : Check.DIVISION_FLOAT_BY_ZERO,
                    getLocation());

            // compiler.addInstruction(new LOAD(memLeft.getDVal(compiler),
            // register.getRegister(compiler)));
            if (getLeftOperand().getType().isInt()) {
                compiler.addInstruction(new QUO(memRight.getDVal(compiler), register.getRegister(compiler)));
            } else {
                compiler.addInstruction(new DIV(memRight.getDVal(compiler), register.getRegister(compiler)));
            }

            ExecutionError.check(compiler, null, Check.OVERFLOW, getLocation());
        }

        return register.withNotify();
    }

}
