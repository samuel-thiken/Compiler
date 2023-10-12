package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.memory.MemoryLocation;
import fr.ensimag.ima.pseudocode.instructions.FLOAT;

import java.util.ArrayList;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ArrType;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 * Conversion of an int into a float. Used for implicit conversions.
 * 
 * @author gl20
 * @date 01/01/2023
 */
public class ConvFloat extends AbstractUnaryExpr {
    public ConvFloat(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        ArrayList<AbstractExpr> index = new ArrayList<AbstractExpr>();
        String symbolName = "float";
        Type newType = compiler.environmentType.FLOAT;
        ListExpr childrenList = new ListExpr();

        if (getOperand() instanceof ArrayExpr) {
            for (AbstractExpr child : ((ArrayExpr) getOperand()).getChildren().getList()) {
                if (child.getType().isFloat())
                    childrenList.add(child);
                else if ((child.getType() instanceof ArrType) && ((ArrType) child.getType()).isFloatArray())
                    childrenList.add(child);
                else {
                    child = new ConvFloat(child);
                    child.verifyExpr(compiler, localEnv, currentClass);
                    childrenList.add(child);
                }
            }
            ((ArrayExpr) getOperand()).setChildren(childrenList);
        }

        Type type = getOperand().getType();
        while (type instanceof ArrType) {
            index.add(((ArrType) type).getSize());
            symbolName = symbolName + "[]";
            newType = new ArrType(compiler.symbolTable.create(symbolName), new IntLiteral(0), newType);
            type = ((ArrType) type).getChildType();
        }

        type = newType;
        for (AbstractExpr indice : index) {
            ((ArrType) type).setSize(indice);
        }

        setType(newType);
        return getType();
    }

    @Override
    protected String getOperatorName() {
        return "/* conv float */";
    }

    @Override
    public MemoryLocation.Register codeGenEvaluate(DecacCompiler compiler) {
        MemoryLocation.Register register;
        try (MemoryLocation memOperand = getOperand().codeGenEvaluate(compiler);) {
            MemoryLocation[] locations = MemoryLocation.Register.getUnused(compiler, memOperand, null);
            register = (MemoryLocation.Register) locations[0];

            compiler.addInstruction(new FLOAT(register.getDVal(compiler), register.getRegister(compiler)));
        }

        return register;
    }

}
