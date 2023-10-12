package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;

public class ListDeclField extends TreeList<AbstractDeclField> {

    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclField i : getList()) {
            i.decompile(s);
            s.print(";");
            s.println();
        }
    }

    /**
     * Implements non-terminal "list_decl_field" of [SyntaxeContextuelle] in pass 2
     * 
     * @param compiler     contains the "env_types" attribute
     * @param localEnv
     *                     its "parentEnvironment" corresponds to "env_exp_sup"
     *                     attribute
     *                     in precondition, its "current" dictionary corresponds to
     *                     the "env_exp" attribute
     *                     in postcondition, its "current" dictionary corresponds to
     *                     the "env_exp_r" attribute
     * @param currentClass
     *                     corresponds to "class" attribute (null in the main bloc).
     */
    public void verifyListDeclField(DecacCompiler compiler, EnvironmentExp envClass,
            AbstractIdentifier className, AbstractIdentifier superClass) throws ContextualError {
        for (AbstractDeclField i : getList()) {
            i.verifyDeclField(compiler, envClass, className, superClass);
        }
    }

    /**
     * Implements non-terminal "list_decl_field" of [SyntaxeContextuelle] in pass 3
     * 
     * @param compiler   contains the "env_types" attribute
     * @param superClass
     *                   corresponds to "class" attribute (null in the main bloc).
     */
    public void verifyListDeclFieldInit(DecacCompiler compiler, EnvironmentExp envClass,
            AbstractIdentifier currentClass, AbstractIdentifier superClass) throws ContextualError {
        for (AbstractDeclField i : getList()) {
            i.verifyDeclFieldInit(compiler, envClass, currentClass, superClass);

        }
    }

    public void codeGenListDeclField(DecacCompiler compiler) {
        throw new RuntimeException("not implemented yet");
    }
}
