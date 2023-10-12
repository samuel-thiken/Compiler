package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.ExecutionError;
import fr.ensimag.deca.codegen.ExecutionError.Check;
import fr.ensimag.deca.memory.MemoryLocation;
import fr.ensimag.ima.pseudocode.instructions.ADD;

/**
 * @author gl20
 * @date 01/01/2023
 */
public class Plus extends AbstractOpArith {
    public Plus(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected String getOperatorName() {
        return "+";
    }

    @Override
    public MemoryLocation.Register codeGenEvaluate(DecacCompiler compiler) {
        MemoryLocation.Register register;

        compiler.addComment(
                String.format("Addition de %s par %s", getLeftOperand().decompile(), getRightOperand().decompile()));

        try (MemoryLocation memLeft = getLeftOperand().codeGenEvaluate(compiler);
                MemoryLocation memRight = getRightOperand().codeGenEvaluate(compiler);) {
            MemoryLocation[] locations = MemoryLocation.Register.getUnused(compiler, memLeft, memRight);
            register = (MemoryLocation.Register) locations[0];

            compiler.addComment(String.format("Opération addition de %s par %s", getLeftOperand().decompile(),
                    getRightOperand().decompile()));
            // compiler.addInstruction(new LOAD(memLeft.getDVal(compiler),
            // register.getRegister(compiler)));
            compiler.addInstruction(new ADD(locations[1].getDVal(compiler), register.getRegister(compiler)));

            if (getType().isFloat())
                ExecutionError.check(compiler, null, Check.OVERFLOW, getLocation());
        }

        return register.withNotify();
    }
}
