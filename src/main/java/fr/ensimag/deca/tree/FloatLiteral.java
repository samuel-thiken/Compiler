package fr.ensimag.deca.tree;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.memory.MemoryLocation;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.ImmediateFloat;
import fr.ensimag.ima.pseudocode.instructions.WFLOAT;
import fr.ensimag.ima.pseudocode.instructions.WFLOATX;

/**
 * Single precision, floating-point literal
 *
 * @author gl20
 * @date 01/01/2023
 */
public class FloatLiteral extends AbstractLiteral {

    public float getValue() {
        return value;
    }

    private float value;

    @Override
    public String getStringRepresentation() {
        return Float.toString(value);
    }

    public FloatLiteral(float value) {
        Validate.isTrue(!Float.isInfinite(value),
                "literal values cannot be infinite");
        Validate.isTrue(!Float.isNaN(value),
                "literal values cannot be NaN");
        this.value = value;

        setMemoryLocation(MemoryLocation.Immediate.create(this, new ImmediateFloat(value)));
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        setType(compiler.environmentType.FLOAT);
        return getType();        
    }


    @Override
    public void decompile(IndentPrintStream s) {
        s.print(java.lang.Float.toString(value));
    }

    @Override
    String prettyPrintNode() {
        return "Float (" + getValue() + ")";
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

    @Override
    protected void codeGenPrint(DecacCompiler compiler, boolean hex) {
        try (MemoryLocation register = codeGenEvaluate(compiler).toRegister(compiler, 1);) {
            compiler.addInstruction(hex ? new WFLOATX() : new WFLOAT());
        }
    }

}
