package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.ExecutionError;
import fr.ensimag.deca.codegen.ExecutionError.Check;
import fr.ensimag.deca.memory.MemoryLocation;
import fr.ensimag.ima.pseudocode.instructions.SUB;

/**
 * @author gl20
 * @date 01/01/2023
 */
public class Minus extends AbstractOpArith {
    public Minus(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected String getOperatorName() {
        return "-";
    }

    @Override
    public MemoryLocation.Register codeGenEvaluate(DecacCompiler compiler) {
        MemoryLocation.Register register;

        compiler.addComment(String.format("Soustraction de %s par %s", getLeftOperand().decompile(),
                getRightOperand().decompile()));
        try (MemoryLocation memLeft = getLeftOperand().codeGenEvaluate(compiler);
                MemoryLocation memRight = getRightOperand().codeGenEvaluate(compiler);) {
            MemoryLocation[] locations = MemoryLocation.Register.getUnused(compiler, memLeft, memRight, true);
            register = (MemoryLocation.Register) locations[0];

            compiler.addComment(String.format("Opération soustraction de %s par %s", getLeftOperand().decompile(),
                    getRightOperand().decompile()));
            // compiler.addInstruction(new LOAD(memLeft.getDVal(compiler),
            // register.getRegister(compiler)));
            compiler.addInstruction(new SUB(memRight.getDVal(compiler), register.getRegister(compiler)));

            if (getType().isFloat())
                ExecutionError.check(compiler, null, Check.OVERFLOW, getLocation());
        }

        return register;
    }

}
