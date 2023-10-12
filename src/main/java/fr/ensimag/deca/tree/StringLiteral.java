package fr.ensimag.deca.tree;

import java.io.PrintStream;
import java.util.Arrays;

import org.apache.commons.lang.Validate;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.ImmediateString;
import fr.ensimag.ima.pseudocode.instructions.WSTR;

/**
 * String literal
 *
 * @author gl20
 * @date 01/01/2023
 */
public class StringLiteral extends AbstractStringLiteral {

    @Override
    public String getValue() {
        String result = value.substring(1, value.length() - 1); // Suppression des guillemets
        result = result.replace("\\\"", "\""); // (\") -> (")
        result = result.replace("\\\\", "\\"); // (\\) -> (\)
        return result;
    }

    private String value;

    @Override
    public String getStringRepresentation() {
        return value;
    }

    public StringLiteral(String value) {
        Validate.notNull(value);
        this.value = value;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        setType(compiler.environmentType.STRING); 
        return getType();
    }

    @Override
    protected void codeGenPrint(DecacCompiler compiler, boolean hex) {
        compiler.addInstruction(new WSTR(new ImmediateString(getValue())));
    }

    @Override
    public void decompile(IndentPrintStream s) {
        if (value.contains("\n")) {
            s.print("\"");
            s.indent();
            Arrays.asList(value.substring(1, value.length() - 1).split("\n")).stream().map(line -> line.replaceAll("^ *", "")).filter(line -> line.length() > 0).forEach((line) -> {
                s.println();
                s.print(line);
            });
            s.unindent();
            s.println();
            s.print("\"");
        } else {
            s.print(value);
        }
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
    String prettyPrintNode() {
        return "StringLiteral (" + value + ")";
    }

}
