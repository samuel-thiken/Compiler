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
public class Or extends AbstractOpBool {

    public Or(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected String getOperatorName() {
        return "||";
    }

    private static int orLabelIncrement = 0;

    @Override
    public MemoryLocation codeGenEvaluate(DecacCompiler compiler) {
        Label labelFinOr = new Label(String.format("fin_or_%d_%d_%d", getLocation().getLine(),
                getLocation().getPositionInLine(), orLabelIncrement++));
        MemoryLocation.Immediate one = MemoryLocation.Immediate.create(null, new ImmediateInteger(1));

        // On évalue memLeft
        MemoryLocation memLeft = getLeftOperand().codeGenEvaluate(compiler);
        MemoryLocation.Register register = MemoryLocation.Register.create(compiler);
        // Si memLeft est vrai, on peut sauter les autres évaluations
        compiler.addInstruction(new LOAD(memLeft.getDVal(compiler), register.getRegister(compiler)));
        compiler.addInstruction(new CMP(one.getDVal(compiler), register.getRegister(compiler)));
        compiler.addInstruction(new BEQ(labelFinOr));
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

        compiler.addLabel(labelFinOr);

        return register;
    }

}
