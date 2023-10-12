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
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BNE;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.CMP;

/**
 * Full if/else if/else statement.
 *
 * @author gl20
 * @date 01/01/2023
 */
public class IfThenElse extends AbstractInst {

    private final AbstractExpr condition;
    private final ListInst thenBranch;
    private ListInst elseBranch;

    public IfThenElse(AbstractExpr condition, ListInst thenBranch, ListInst elseBranch) {
        Validate.notNull(condition);
        Validate.notNull(thenBranch);
        Validate.notNull(elseBranch);
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    /**
     * Set the current else branch to the given parameter elseBranch
     * 
     * @author H
     * @param elseBranch
     */
    public void setElseBranch(ListInst elseBranch) {
        this.elseBranch = elseBranch;
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        condition.verifyExpr(compiler, localEnv, currentClass);
        condition.verifyCondition(compiler, localEnv, currentClass);
        thenBranch.verifyListInst(compiler, localEnv, currentClass, returnType);
        elseBranch.verifyListInst(compiler, localEnv, currentClass, returnType);
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        String id = String.format("_%d_%d", getLocation().getLine(), getLocation().getPositionInLine());
        Label labelElse = new Label("else" + id);
        Label labelFinIf = new Label("fin_if" + id);

        compiler.addComment(String.format("Debut condition if%s", id));
        try (MemoryLocation.Register memCondition = condition.codeGenEvaluate(compiler).toRegister(compiler);
                MemoryLocation one = MemoryLocation.Immediate.create(null, new ImmediateInteger(1))) {
            compiler.addInstruction(new CMP(one.getDVal(compiler), memCondition.getRegister(compiler)));
            compiler.addInstruction(new BNE(labelElse));
        }

        compiler.getMemoryManager().startConditionalBlock();
        // Contenu du if
        compiler.addComment(String.format("Debut if%s", id));
        thenBranch.codeGenListInst(compiler);
        // compiler.getMemoryManager().invalidateAll();
        compiler.getMemoryManager().endConditionalBlock();

        compiler.addInstruction(new BRA(labelFinIf));
        compiler.addLabel(labelElse);

        compiler.getMemoryManager().startConditionalBlock();
        // Contenu du else
        elseBranch.codeGenListInst(compiler);
        // compiler.getMemoryManager().invalidateAll();
        compiler.getMemoryManager().endConditionalBlock();

        compiler.addLabel(labelFinIf);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("if(");
        condition.decompile(s);
        s.println(") {");
        s.indent();
        thenBranch.decompile(s);
        s.unindent();
        s.print("} ");
        if (!elseBranch.isEmpty()) {
            s.print("else {");
            s.println();
            s.indent();
            elseBranch.decompile(s);
            s.unindent();
            s.print("}");
        }
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        condition.iter(f);
        thenBranch.iter(f);
        elseBranch.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        condition.prettyPrint(s, prefix, false);
        thenBranch.prettyPrint(s, prefix, false);
        elseBranch.prettyPrint(s, prefix, true);
    }
}
