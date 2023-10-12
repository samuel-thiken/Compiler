package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ArrType;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.memory.MemoryLocation;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.instructions.BNE;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.NEW;
import fr.ensimag.ima.pseudocode.instructions.STORE;

/**
 * Assignment, i.e. lvalue = expr.
 *
 * @author gl20
 * @date 01/01/2023
 */
public class Assign extends AbstractBinaryExpr {

    @Override
    public AbstractLValue getLeftOperand() {
        // The cast succeeds by construction, as the leftOperand has been set
        // as an AbstractLValue by the constructor.
        return (AbstractLValue) super.getLeftOperand();
    }

    public Assign(AbstractLValue leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    /**
     * Returns the type of the literal obtained by the binary operation.
     * 
     * @author H
     */
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type leftOpType = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type rightOpType = getRightOperand().verifyExpr(compiler, localEnv, currentClass);
        try {
            setRightOperand(getRightOperand().verifyRValue(compiler, localEnv, currentClass, leftOpType));
        } catch (ContextualError e) {
            throw new ContextualError(String.format(ContextualError.INCOMPATIBLE_TYPE_ASSIGN,
                    rightOpType.getName(), leftOpType.getName()),
                    getLocation());
        }
        setType(leftOpType);
        return getType();
    }

    @Override
    protected String getOperatorName() {
        return "=";
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        compiler.addComment(String.format("Assignation de %s", decompile()));
        MemoryLocation register = codeGenEvaluate(compiler);
        register.close();
    }

    @Override
    public MemoryLocation codeGenEvaluate(DecacCompiler compiler) {
        MemoryLocation.Register register;
        try (MemoryLocation.Address memLeft = getLeftOperand().codeGenEvaluate(compiler).getAddress();) {
            if (getRightOperand() instanceof ArrayExpr) {
                ArrayExpr arrayExpr = ((ArrayExpr) getRightOperand());
                if (!(((ArrType) getLeftOperand().getType()).getSize() instanceof EmptyExpr)) {
                    arrayExpr.codeGenVerifySizes(compiler, (ArrType) getLeftOperand().getType());
                }
                // Vérification de l'initialisation du tableau
                Label alreadyInitializedLabel = new Label(String.format("already_initialized_tab_%d_%d",
                        getLocation().getLine(), getLocation().getPositionInLine()));
                compiler.addInstruction(new CMP(new NullOperand(), memLeft.toRegister(compiler).getRegister(compiler)));
                compiler.addInstruction(new BNE(alreadyInitializedLabel));
                int totalSize = arrayExpr.getTotalSize();
                try (MemoryLocation.Register addrRegister = MemoryLocation.Register.create(compiler)) {
                    compiler.addInstruction(
                            new NEW(new ImmediateInteger(totalSize), addrRegister.getRegister(compiler)));
                    // storing the address inside 'register' in the stack at the address of the
                    // variable
                    compiler.addInstruction(
                            new STORE(addrRegister.getRegister(compiler), memLeft.getRegisterOffset(compiler)));
                }
                compiler.addLabel(alreadyInitializedLabel);

                compiler.addComment("Stockage des données du tableau");
                arrayExpr.codeGenInitRecSize(compiler, memLeft.toRegister(compiler), 0);
                compiler.addComment("Stockage des valeurs du tableau");
                arrayExpr.codeGenInit(compiler, memLeft, null);
                register = memLeft.toRegister(compiler);
            } else {
                register = getRightOperand().codeGenEvaluate(compiler).toRegister(compiler);
                compiler.addInstruction(
                        new STORE(register.getRegister(compiler), (DAddr) memLeft.getDVal(compiler)));

                register.changeReference(memLeft);
            }
        }
        return register;
    }

}
