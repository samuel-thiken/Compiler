package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.memory.MemoryLocation;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

/**
 *
 * @author gl20
 * @date 01/01/2023
 */
public class And extends AbstractOpBool {

    public And(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected String getOperatorName() {
        return "&&";
    }

    private static int andLabelIncrement = 1;

    @Override
    public MemoryLocation codeGenEvaluate(DecacCompiler compiler) {
        Label labelFinAnd = new Label(
                String.format("fin_and_%d_%d_%d", getLocation().getLine(), getLocation().getPositionInLine(),
                        andLabelIncrement++));
        MemoryLocation.Immediate zero = MemoryLocation.Immediate.create(null, new ImmediateInteger(0));

        // On évalue memLeft
        MemoryLocation memLeft = getLeftOperand().codeGenEvaluate(compiler);
        MemoryLocation[] locations = MemoryLocation.Register.getUnused(compiler, memLeft, null);
        MemoryLocation.Register register = (MemoryLocation.Register) locations[0];
        // Si memLeft est false, on peut sauter les autres évaluations
        // compiler.addInstruction(new LOAD(memLeft.getDVal(compiler), register.getRegister(compiler)));
        compiler.addInstruction(new CMP(zero.getDVal(compiler), register.getRegister(compiler)));
        compiler.addInstruction(new BEQ(labelFinAnd));
        // On libère memLeft
        memLeft.close();

        compiler.getMemoryManager().startConditionalBlock();
        // On évalue memRight
        MemoryLocation memRight = getRightOperand().codeGenEvaluate(compiler);
        // On retourne la valeur de memRight
        compiler.addInstruction(new LOAD(memRight.getDVal(compiler), register.getRegister(compiler)));
        // On libère memRight
        memRight.close();
        compiler.getMemoryManager().endConditionalBlock();

        compiler.addLabel(labelFinAnd);

        return register;
    }

}
