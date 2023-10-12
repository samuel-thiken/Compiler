package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.memory.MemoryLocation;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BNE;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.CMP;

import java.io.PrintStream;
import org.apache.commons.lang.Validate;

/**
 *
 * @author gl20
 * @date 01/01/2023
 */
public class While extends AbstractInst {
    private AbstractExpr condition;
    private ListInst body;

    public AbstractExpr getCondition() {
        return condition;
    }

    public ListInst getBody() {
        return body;
    }

    public While(AbstractExpr condition, ListInst body) {
        Validate.notNull(condition);
        Validate.notNull(body);
        this.condition = condition;
        this.body = body;
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        String id = String.format("_%d_%d", getLocation().getLine(), getLocation().getPositionInLine());
        Label labelDebutWhile = new Label("debut_while" + id);
        Label labelFinWhile = new Label("fin_while" + id);

        // Cas particulier : on doit charger la valeur de la condition à chaque boucle, donc après le label début_while
        // Les registres sont invalides lorsqu'on remonte au début du while, donc on doit invalider les registres
        compiler.getMemoryManager().invalidateAll();

        compiler.addLabel(labelDebutWhile);
        compiler.addComment("Debut condition du while");

        try (MemoryLocation.Register memCondition = condition.codeGenEvaluate(compiler).toRegister(compiler);
        MemoryLocation one = MemoryLocation.Immediate.create(null, new ImmediateInteger(1));) {
        compiler.addInstruction(new CMP(one.getDVal(compiler), memCondition.getRegister(compiler)));
        compiler.addInstruction(new BNE(labelFinWhile));
        }
        compiler.addComment("Fin condition du while");
        
        // Contenu du while
        compiler.getMemoryManager().invalidateAll();
        body.codeGenListInst(compiler);

        compiler.addComment("Fin du while");
        compiler.addInstruction(new BRA(labelDebutWhile));
        compiler.addLabel(labelFinWhile);
        
        // Après le while, les registres ne sont pas forcément bons (modifiés lors de l'évaluation de la condition)
        // Dans le doute, on invalide les registres
        compiler.getMemoryManager().invalidateAll();
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        condition.verifyExpr(compiler, localEnv, currentClass);
        condition.verifyCondition(compiler, localEnv, currentClass);
        body.verifyListInst(compiler, localEnv, currentClass, returnType);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("while (");
        getCondition().decompile(s);
        s.println(") {");
        s.indent();
        getBody().decompile(s);
        s.unindent();
        s.print("}");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        condition.iter(f);
        body.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        condition.prettyPrint(s, prefix, false);
        body.prettyPrint(s, prefix, true);
    }

}
