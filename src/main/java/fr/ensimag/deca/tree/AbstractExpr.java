package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ArrType;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.memory.MemoryLocation;
import fr.ensimag.deca.memory.MemoryLocation.Register;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.ima.pseudocode.instructions.WFLOAT;
import fr.ensimag.ima.pseudocode.instructions.WFLOATX;
import fr.ensimag.ima.pseudocode.instructions.WINT;

/**
 * Expression, i.e. anything that has a value.
 *
 * @author gl20
 * @date 01/01/2023
 */
public abstract class AbstractExpr extends AbstractInst {

    private MemoryLocation memoryLocation;

    public void setMemoryLocation(MemoryLocation memoryLocation) {
        this.memoryLocation = memoryLocation;
    }

    public MemoryLocation getMemoryLocation(DecacCompiler compiler) {
        return memoryLocation;
    }

    /**
     * @return true if the expression does not correspond to any concrete token
     *         in the source code (and should be decompiled to the empty string).
     */
    boolean isImplicit() {
        return false;
    }

    /**
     * Get the type decoration associated to this expression (i.e. the type computed
     * by contextual verification).
     */
    public Type getType() {
        return type;
    }

    protected void setType(Type type) {
        Validate.notNull(type);
        this.type = type;
    }

    private Type type;

    @Override
    protected void checkDecoration() {
        if (getType() == null) {
            throw new DecacInternalError("Expression " + decompile() + " has no Type decoration");
        }
    }

    /**
     * Verify the expression for contextual error.
     * 
     * implements non-terminals "expr" and "lvalue"
     * of [SyntaxeContextuelle] in pass 3
     *
     * @param compiler     (contains the "env_types" attribute)
     * @param localEnv
     *                     Environment in which the expression should be checked
     *                     (corresponds to the "env_exp" attribute)
     * @param currentClass
     *                     Definition of the class containing the expression
     *                     (corresponds to the "class" attribute)
     *                     is null in the main bloc.
     * @return the Type of the expression
     *         (corresponds to the "type" attribute)
     */
    public abstract Type verifyExpr(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError;

    /**
     * Verify the expression in right hand-side of (implicit) assignments
     * 
     * implements non-terminal "rvalue" of [SyntaxeContextuelle] in pass 3
     *
     * @author H
     * @param compiler     contains the "env_types" attribute
     * @param localEnv     corresponds to the "env_exp" attribute
     * @param currentClass corresponds to the "class" attribute
     * @param expectedType corresponds to the "type1" attribute
     * @return this with an additional ConvFloat if needed...
     */
    public AbstractExpr verifyRValue(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass,
            Type expectedType)
            throws ContextualError {
        Type typeExpr = verifyExpr(compiler, localEnv, currentClass);
        boolean testArray = (typeExpr instanceof ArrType) && (expectedType instanceof ArrType)
                && ((ArrType) typeExpr).elementType().isInt() && ((ArrType) expectedType).elementType().isFloat();
        if (!typeExpr.sameType(expectedType)) {
            if ((typeExpr.isInt() && expectedType.isFloat()) || testArray) { // if the current type is an int and the
                                                                             // expected type is
                // a float, we can use the implicit cast convFloat
                ConvFloat convertedInt = new ConvFloat(this);
                convertedInt.verifyExpr(compiler, localEnv, currentClass);
                return convertedInt;
            }
        }

        if (!expectedType.isAssignCompatible(typeExpr))
            throw new ContextualError(
                    String.format(ContextualError.INVALID_CAST, getType().getName(), expectedType.getName()),
                    getLocation());

        if (typeExpr.isInt() && expectedType.isFloat()) { // if the current type is an int and the expected type is
                                                          // a float, we can use the implicit cast convFloat
            ConvFloat convertedInt = new ConvFloat(this);
            convertedInt.verifyExpr(compiler, localEnv, currentClass);
            return convertedInt;
        }
        return this;
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        verifyExpr(compiler, localEnv, currentClass);
    }

    /**
     * Verify the expression as a condition, i.e. check that the type is
     * boolean.
     *
     * @param localEnv
     *                     Environment in which the condition should be checked.
     * @param currentClass
     *                     Definition of the class containing the expression, or
     *                     null in
     *                     the main program.
     */
    void verifyCondition(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        if (!getType().isBoolean())
            throw new ContextualError(String.format(ContextualError.NOT_A_BOOLEAN_CONDITION, decompile()),
                    getLocation());
    }

    /**
     * Generate code to print the expression
     *
     * @param compiler
     */
    protected void codeGenPrint(DecacCompiler compiler, boolean hex) {
        try (MemoryLocation mem = codeGenEvaluate(compiler).toRegister(compiler, 1)) {
            if (getType().isInt())
                compiler.addInstruction(new WINT());
            else
                compiler.addInstruction(hex ? new WFLOATX() : new WFLOAT());
        }
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public MemoryLocation codeGenEvaluate(DecacCompiler compiler) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    protected void decompileInst(IndentPrintStream s) {
        decompile(s);
    }

    public fr.ensimag.deca.memory.MemoryLocation.Register codeGenInit(DecacCompiler compiler, DAddr addr, Integer tempRegisterIndex) {
        return null;
    }


    @Override
    protected void prettyPrintType(PrintStream s, String prefix) {
        Type t = getType();
        if (t != null) {
            s.print(prefix);
            s.print("type: ");
            s.print(t);
            s.println();
        }
    }

    /**
     * Initialize values of an array in the heap memory
     * @param compiler
     * @param addr
     */
    protected void initialization(DecacCompiler compiler, Register addr, int offset) {
        AbstractExpr currentExpr = this;
        if (!(currentExpr instanceof ArrayExpr)) { // leaf node

            // storing the value of the node to the correct address in the heap (which is the parameter 'addr')
            MemoryLocation.Register registerValue = currentExpr.codeGenEvaluate(compiler).toRegister(compiler);
            compiler.addInstruction(new STORE(registerValue.getRegister(compiler), addr.getAddress().getRegisterOffset(compiler)));
            return ;
        }
        ArrayExpr arrExpr = (ArrayExpr) currentExpr;
        for (int i = 0; i < arrExpr.getNbOfEl(); i++) {

            compiler.addComment("initialisation");
            
            // incrementing the offset of the address in the heap
            offset++;

            // initialization of the children
            (arrExpr.getChildren().getList().get(i)).initialization(compiler, addr, offset);
        }
    }
}
